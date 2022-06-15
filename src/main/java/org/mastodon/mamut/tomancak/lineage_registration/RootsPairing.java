package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.HashSet;
import java.util.Set;

import org.mastodon.collection.ObjectRefMap;
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
	 * Pair the root nodes in two ModelGraphs based on their label.
	 * A root node in graphA is paired with a root node in graphB
	 * if it has the same label.
	 * 
	 * @param graphA graph A
	 * @param graphB graph B   
	 * @return a map, that maps root nodes in graph A to equally named
	 * root nodes of graph B.
	 */
	static RefRefMap< Spot, Spot > pairRoots( ModelGraph graphA, ModelGraph graphB )
	{
		ObjectRefMap< String, Spot > rootsA = dividingRootsMap( graphA );
		ObjectRefMap< String, Spot > rootsB = dividingRootsMap( graphB );
		Set< String > intersection = intersection( rootsA.keySet(), rootsB.keySet() );
		RefRefMap< Spot, Spot > roots = new RefRefHashMap<>( graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
		for( String label : intersection )
			roots.put( rootsA.get( label ), rootsB.get( label ) );
		return roots;
	}

	/**
	 * @return a map form "root label" to "root node" for the given graph.
	 * The map only contains those roots that actually divide.
	 */
	private static ObjectRefMap< String, Spot> dividingRootsMap( ModelGraph graphA )
	{
		RefSet< Spot > roots = LineageTreeUtils.getRoots( graphA );
		ObjectRefMap< String, Spot > map = new ObjectRefHashMap<>( graphA.vertices().getRefPool() );
		for ( Spot spot : roots )
			if ( LineageTreeUtils.doesBranchDivide( graphA, spot ) )
				map.put( spot.getLabel(), spot );
		return map;
	}

	/** @return the intersection of two sets. */
	private static <T> Set< T > intersection( Set< T > a, Set< T > b )
	{
		Set<T> intersection = new HashSet<>( a );
		intersection.retainAll( b );
		return intersection;
	}
}
