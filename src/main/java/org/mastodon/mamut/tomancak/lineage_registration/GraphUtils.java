package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

public class GraphUtils
{
	/**
	 * @return the set of root nodes of the given graph.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > RefSet< V > getRoots( ReadOnlyGraph< V, E > graph )
	{
		Predicate< V > isRoot = spot -> spot.incomingEdges().isEmpty();
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > RefSet< V > getRoots( ReadOnlyGraph< V, E > graph, int timepoint )
	{
		Predicate< V > isRoot = spot -> spot.getTimepoint() == timepoint || ( spot.incomingEdges().isEmpty() && spot.getTimepoint() > timepoint );
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	/**
	 * @return true if the given spot is part of a branch that divides.
	 */
	public static boolean doesBranchDivide( final Spot spot, final Spot ref )
	{
		Spot branchEnd = BranchGraphUtils.getBranchEnd( spot, ref );
		return branchEnd.outgoingEdges().size() > 1;
	}
}
