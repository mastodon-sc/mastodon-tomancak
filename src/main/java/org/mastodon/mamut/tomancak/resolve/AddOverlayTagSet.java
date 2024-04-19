package org.mastodon.mamut.tomancak.resolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.Pair;
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
import org.mastodon.mamut.tomancak.lineage_registration.Glasbey;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.util.TagSetUtils;
import org.mastodon.util.TreeUtils;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class AddOverlayTagSet
{
	public static void run( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			final RefIntMap< Spot > branchIds = getBranchIdMap( model );
			final Map< TIntSet, RefList< Spot > > conflictGroups = computeConflictGroups( model, branchIds );
			addTagSets( model, conflictGroups );
		}
		finally
		{
			lock.unlock();
		}
		// do we need: graph.notifyGraphChanged();
	}

	private static Map< TIntSet, RefList< Spot > > computeConflictGroups( Model model, RefIntMap< Spot > branchIds )
	{
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		final int minTimepoint = TreeUtils.getMinTimepoint( model );
		final Map< TIntSet, RefList< Spot > > conflictGroups = new HashMap<>();
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > frame = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( final Set< Spot > conflict : findConflicts( model.getGraph(), frame ) )
				addConflict( model, conflict, branchIds, conflictGroups );
		}
		return conflictGroups;
	}

	private static void addConflict( final Model model, final Set< Spot > conflict, final RefIntMap< Spot > branchIds, final Map< TIntSet, RefList< Spot > > conflictGroups )
	{
		final TIntSet conflictBranchIds = mapToBranchIds( conflict, branchIds );
		RefList< Spot > sets = conflictGroups.get( conflictBranchIds );
		if ( sets == null )
		{
			sets = new RefArrayList<>( model.getGraph().vertices().getRefPool() );
			conflictGroups.put( conflictBranchIds, sets );
		}
		sets.addAll( conflict );
	}

	private static void addTagSets( final Model model, final Map< TIntSet, RefList< Spot > > conflictGroups )
	{

		final ArrayList< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		final int num = conflictGroups.size();
		for ( int i = 0; i < num; i++ )
			tagsAndColors.add( Pair.of( "Conflict " + i, Glasbey.GLASBEY[ i + 4 ] ) );
		final TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "Overlapping Spots", tagsAndColors );
		final List< TagSetStructure.Tag > tags = tagSet.getTags();
		final List< RefList< Spot > > conflicts = new ArrayList<>( conflictGroups.values() );
		for ( int i = 0; i < conflicts.size(); i++ )
			TagSetUtils.tagSpots( model, tagSet, tags.get( i ), conflicts.get( i ) );
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

	private static List< Set< Spot > > findConflicts( final ModelGraph graph, final SpatialIndex< Spot > frame )
	{
		final double threshold = 0.4;
		final IncrementalNearestNeighborSearch< Spot > nearestNeighbors = frame.getIncrementalNearestNeighborSearch();
		final RefObjectMap< Spot, Set< Spot > > conflicts = new RefObjectHashMap<>( graph.vertices().getRefPool() );
		for ( final Spot spot : frame )
		{
			final RefSet< Spot > group = findOverlaps( graph, spot, nearestNeighbors, threshold );
			if ( group != null && !group.isEmpty() )
			{
				group.add( spot );
				mergeGroups( group, conflicts );
			}
		}
		return new ArrayList<>( conflicts.values() );
	}

	private static RefSet< Spot > findOverlaps( final ModelGraph graph, final Spot spot, final IncrementalNearestNeighborSearch< Spot > nearestNeighbors, final double threshold )
	{
		RefSet< Spot > group = null;
		nearestNeighbors.search( spot );
		for ( int i = 0; i < 10; i++ )
		{
			if ( !nearestNeighbors.hasNext() )
				break;
			final Spot neighbor = nearestNeighbors.next();
			if ( spot.equals( neighbor ) )
				continue;
			final boolean overlap = HellingerDistance.hellingerDistance( spot, neighbor ) < threshold;
			if ( overlap )
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
		for ( final Spot a : group )
		{
			final Set< Spot > c = conflicts.get( a );
			if ( c != null )
				group.addAll( c );
		}
		for ( final Spot a : group )
			conflicts.put( a, group );
	}
}
