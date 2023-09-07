package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;

import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class LineageTreeUtils
{
	/**
	 * @return the set of root nodes of the given graph.
	 */
	public static RefSet< Spot > getRoots( ModelGraph graph )
	{
		Predicate< Spot > isRoot = spot -> spot.incomingEdges().isEmpty();
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	public static RefSet< Spot > getRoots( ModelGraph graph, int timepoint )
	{
		Predicate< Spot > isRoot = spot -> spot.getTimepoint() == timepoint
				|| ( spot.incomingEdges().isEmpty() && spot.getTimepoint() > timepoint );
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
