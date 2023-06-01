package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.collection.ObjectRefMap;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.ObjectRefHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Pair the root spots in two ModelGraphs based on their label.
 */
public class RootsPairing
{
	/**
	 * <p>
	 * Pair the root nodes in two ModelGraphs based on their label.
	 * A root node in graphA is paired with a root node in graphB
	 * if it has the same label.
	 * </p>
	 * <p>
	 * Important warning: The pairing only contains those root nodes
	 * that actually divide.
	 * </p>
	 * 
	 * @param graphA graph A
	 * @param graphB graph B   
	 * @return a map, that maps root nodes in graph A to equally named
	 * root nodes of graph B.
	 */
	static RefRefMap< Spot, Spot > pairDividingRoots( ModelGraph graphA, int timepointA, ModelGraph graphB, int timepointB )
	{
		RefSet< Spot > rootsA = getRoots( graphA, timepointA );
		RefSet< Spot > rootsB = getRoots( graphB, timepointB );
		return pairSpotsBasedOnLabel( rootsA, rootsB );
	}

	private static RefSet< Spot > getRoots( ModelGraph graph, int timepoint )
	{
		return getBranchStarts( filterDividingSpots( LineageTreeUtils.getRoots( graph, timepoint ) ) );
	}

	private static RefRefMap< Spot, Spot > pairSpotsBasedOnLabel( RefSet< Spot > spotsA, RefSet< Spot > spotsB )
	{
		ObjectRefMap< String, Spot > labelToSpotsA = createLabelToSpotMap( spotsA );
		ObjectRefMap< String, Spot > labelToSpotsB = createLabelToSpotMap( spotsB );
		RefRefMap< Spot, Spot > roots =
				new RefRefHashMap<>( RefCollectionUtils.getRefPool( spotsA ), RefCollectionUtils.getRefPool( spotsB ) );
		for ( String label : intersection( labelToSpotsA.keySet(), labelToSpotsB.keySet() ) )
			roots.put( labelToSpotsA.get( label ), labelToSpotsB.get( label ) );
		return roots;
	}

	private static RefSet< Spot > filterDividingSpots( RefSet< Spot > spots )
	{
		Spot ref = spots.createRef();
		try
		{
			return RefCollectionUtils.filterSet( spots, spot -> LineageTreeUtils.doesBranchDivide( spot, ref ) );
		}
		finally
		{
			spots.releaseRef( ref );
		}
	}

	private static RefSet< Spot > getBranchStarts( RefSet< Spot > spots )
	{
		Spot ref = spots.createRef();
		try
		{
			return RefCollectionUtils.applySet( spots, spot -> BranchGraphUtils.getBranchStart( spot, ref ) );
		}
		finally
		{
			spots.releaseRef( ref );
		}

	}

	private static ObjectRefMap< String, Spot > createLabelToSpotMap( RefSet< Spot > dividingRoots )
	{
		RefPool< Spot > spotRefPool = Objects.requireNonNull( RefCollections.tryGetRefPool( dividingRoots ) );
		ObjectRefMap< String, Spot > map = new ObjectRefHashMap<>( spotRefPool );
		for ( Spot spot : dividingRoots )
			map.put( spot.getLabel(), spot );
		return map;
	}

	/** @return the intersection of two sets. */
	private static < T > Set< T > intersection( Set< T > a, Set< T > b )
	{
		Set< T > intersection = new HashSet<>( a );
		intersection.retainAll( b );
		return intersection;
	}

	public static String report( ModelGraph graphA, int firstTimepointA, ModelGraph graphB, int firstTimepointB )
	{
		Set< String > rootsA = createLabelToSpotMap( getRoots( graphA, firstTimepointA ) ).keySet();
		Set< String > rootsB = createLabelToSpotMap( getRoots( graphB, firstTimepointB ) ).keySet();
		return "Roots found in the first dataset:\n"
				+ "   " + rootsA + "\n"
				+ "Roots found in the second dataset:\n"
				+ "   " + rootsB + "\n"
				+ "Roots found in both datasets:\n"
				+ "   " + intersection( rootsA, rootsB ) + "\n";
	}
}
