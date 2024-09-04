package org.mastodon.mamut.tomancak.resolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.tomancak.util.Glasbey;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.util.TagSetUtils;
import org.mastodon.util.TreeUtils;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Class that holds the code for creating a conflict tag set. Uses
 * {@link HellingerDistance} to figure out which spots overlap
 * significantly.
 */
public class CreateConflictTagSet
{
	private CreateConflictTagSet()
	{
		// prevent instantiation of utility class.
	}

	/**
	 * Add a new tag set to the model, with all conflicts between spots tagged.
	 * A conflict is defined as two spots whose Hellinger distance is less than the
	 * threshold.
	 *
	 * @param model      the model that contains the spot graph and to which the tag set
	 *                   will be added.
	 * @param tagSetName the name of the tag set to be created.
	 * @param threshold  the Hellinger distance threshold. Two spot are considered in
	 *                   conflict if their Hellinger distance is less than or equal to
	 *                   this value.
	 * @return the tag set that was created.
	 */
	public static TagSetStructure.TagSet run( final Model model, final String tagSetName, final double threshold )
	{
		final ModelGraph graph = model.getGraph();
		TagSetStructure.TagSet tagSet;
		final ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			tagSet = createTagSet( model, tagSetName, threshold );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
		graph.notifyGraphChanged();
		return tagSet;
	}

	private static TagSetStructure.TagSet createTagSet( final Model model, final String tagSetName, final double threshold )
	{
		final RefIntMap< Spot > branchIds = getBranchIdMap( model );
		final Map< TIntSet, RefList< Spot > > conflictGroups = computeConflictGroups( model, branchIds, threshold );
		return addTagSet( model, tagSetName, conflictGroups, branchIds );
	}

	private static Map< TIntSet, RefList< Spot > > computeConflictGroups( final Model model, final RefIntMap< Spot > branchIds, final double threshold )
	{
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		final int minTimepoint = TreeUtils.getMinTimepoint( model );
		final Map< TIntSet, RefList< Spot > > conflictGroups = new HashMap<>();
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > frame = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( final Set< Spot > conflict : findConflictsForFrame( model.getGraph(), frame, threshold ) )
				addConflict( model, conflict, branchIds, conflictGroups );
		}
		return conflictGroups;
	}

	private static void addConflict( final Model model, final Set< Spot > conflict, final RefIntMap< Spot > branchIds, final Map< TIntSet, RefList< Spot > > conflictGroups )
	{
		final TIntSet conflictBranchIds = mapToBranchIds( conflict, branchIds );
		final RefList< Spot > sets = conflictGroups.computeIfAbsent( conflictBranchIds,
				ignore -> new RefArrayList<>( model.getGraph().vertices().getRefPool() ) );
		sets.addAll( conflict );
	}

	private static TagSetStructure.TagSet addTagSet( final Model model, final String tagSetName, final Map< TIntSet, RefList< Spot > > conflictGroups, final RefIntMap< Spot > branchIds )
	{

		final ArrayList< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		final List< TIntSet > keys = new ArrayList<>( conflictGroups.keySet() );
		final IntSupplier colorSupplier = Glasbey.getGlasbeyLightColorSupplier();
		for ( int i = 0; i < keys.size(); i++ )
		{
			final TIntSet key = keys.get( i );
			for ( int k = 0; k < key.size(); k++ )
				tagsAndColors.add( Pair.of( "Conflict " + i + " (" + getLetters( k ) + ")", colorSupplier.getAsInt() ) );
		}
		final TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagsAndColors );
		final List< TagSetStructure.Tag > tags = tagSet.getTags();
		int j = 0;
		for ( final TIntSet key : keys )
		{
			final TIntObjectHashMap< TagSetStructure.Tag > branchIdToTag = new TIntObjectHashMap<>();
			for ( final int branchId : key.toArray() )
				branchIdToTag.put( branchId, tags.get( j++ ) );

			for ( final Spot spot : conflictGroups.get( key ) )
			{
				final TagSetStructure.Tag tag = branchIdToTag.get( branchIds.get( spot ) );
				TagSetUtils.tagSpotAndIncomingEdges( model, spot, tagSet, tag );
			}
		}
		return tagSet;
	}

	/**
	 * Return the (i+1)th entry in a infinite list starting with A, B, ..., Z, AA, AB, ..., AZ, BA, BB, ...
	 */
	static String getLetters( int index )
	{
		if ( index == 0 )
			return "a";
		final StringBuilder sb = new StringBuilder();
		while ( index >= 0 )
		{
			sb.append( ( char ) ( 'a' + index % 26 ) );
			index /= 26;
			index--;
		}
		return sb.reverse().toString();
	}

	private static RefIntMap< Spot > getBranchIdMap( final Model model )
	{
		final ModelBranchGraph branchGraph = new ModelBranchGraph( model.getGraph() );
		final RefIntMap< Spot > branchIds = new RefIntHashMap<>( model.getGraph().vertices().getRefPool(), -1 );
		branchGraph.graphRebuilt();
		final BranchSpot ref = branchGraph.vertexRef();
		for ( final Spot spot : model.getGraph().vertices() )
		{
			final int internalPoolIndex = branchGraph.getBranchVertex( spot, ref ).getInternalPoolIndex();
			branchIds.put( spot, internalPoolIndex );
		}
		return branchIds;
	}

	private static TIntSet mapToBranchIds( final Set< Spot > conflict, final RefIntMap< Spot > branchIds )
	{
		final TIntSet branchIdsInConflict = new TIntHashSet();
		for ( final Spot spot : conflict )
			branchIdsInConflict.add( branchIds.get( spot ) );
		return branchIdsInConflict;
	}

	/**
	 * For the given frame, return a list of sets of spots that are in conflict.
	 * Two spots are in conflict if their Hellinger distance is less than the threshold.
	 */
	private static List< Set< Spot > > findConflictsForFrame( final ModelGraph graph, final SpatialIndex< Spot > frame, final double threshold )
	{
		// This method returns the connected components of the conflict graph.
		// Reimplementing this method with a proper connected components algorithm would be a good idea.
		final IncrementalNearestNeighborSearch< Spot > nearestNeighbors = frame.getIncrementalNearestNeighborSearch();
		final RefObjectMap< Spot, Set< Spot > > conflicts = new RefObjectHashMap<>( graph.vertices().getRefPool() );
		for ( final Spot spot : frame )
		{
			final RefSet< Spot > group = findConflictsForSpot( graph, spot, nearestNeighbors, threshold );
			if ( group != null && !group.isEmpty() )
			{
				group.add( spot );
				mergeGroups( group, conflicts );
			}
		}
		return new ArrayList<>( conflicts.values() );
	}

	/**
	 * Return a set of spots that the given spot is in conflict with.
	 * Two spots are in conflict if their Hellinger distance is less than the threshold.
	 * <p>
	 * For efficiency, we only check the 10 nearest neighbors.
	 */
	private static RefSet< Spot > findConflictsForSpot( final ModelGraph graph, final Spot spot, final IncrementalNearestNeighborSearch< Spot > nearestNeighbors, final double threshold )
	{
		// NB: Having a fixed number of neighbors to check is a bad idea.
		//    This is a quick hack to make the method run in reasonable time.
		//    It would be better to derive a distance threshold from the Hellinger
		//    distance equation, Hellinger distance threshold and the spot radius.
		RefSet< Spot > group = null;
		nearestNeighbors.search( spot );
		int i = 0;
		while ( nearestNeighbors.hasNext() && i < 10 )
		{
			i++;
			final Spot neighbor = nearestNeighbors.next();
			if ( spot.equals( neighbor ) )
				continue;
			final boolean conflict = HellingerDistance.hellingerDistance( spot, neighbor ) < threshold;
			if ( conflict )
			{
				if ( null == group )
					group = new RefSetImp<>( graph.vertices().getRefPool() );
				group.add( neighbor );
			}
		}
		return group;
	}

	private static void mergeGroups( final RefSet< Spot > group, final RefObjectMap< Spot, Set< Spot > > conflicts )
	{
		final RefSet< Spot > combined = RefCollections.createRefSet( group );
		combined.addAll( group );
		for ( final Spot a : group )
		{
			final Set< Spot > c = conflicts.get( a );
			if ( c != null )
				combined.addAll( c );
		}
		for ( final Spot a : combined )
			conflicts.put( a, combined );
	}
}
