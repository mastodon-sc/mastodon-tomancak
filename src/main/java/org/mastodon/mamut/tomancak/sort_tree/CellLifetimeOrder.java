package org.mastodon.mamut.tomancak.sort_tree;

import java.util.Iterator;
import java.util.function.Predicate;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.BranchGraphUtils;

/**
 * A sorting "order" for sorting the lineage tree in a {@link ModelGraph}.
 * Compares the cell lifecycles of the two child cells of a given spot.
 * The child with the longer cell lifecycle is sorted to be the first child.
 */
public class CellLifetimeOrder implements Predicate< Spot >
{
	private final ModelGraph graph;

	public CellLifetimeOrder( ModelGraph graph )
	{
		this.graph = graph;
	}

	/**
	 * Returns true if the cells are correctly sorted.
	 */
	@Override
	public boolean test( Spot spot )
	{
		if ( spot.outgoingEdges().size() != 2 )
			return true;

		Spot ref1 = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		Spot ref3 = graph.vertexRef();
		Spot ref4 = graph.vertexRef();
		try
		{
			Iterator< Link > iterator = spot.outgoingEdges().iterator();
			Spot child1 = iterator.next().getTarget( ref1 );
			Spot child2 = iterator.next().getTarget( ref2 );
			Spot end1 = BranchGraphUtils.getBranchEnd( child1, ref3 );
			Spot end2 = BranchGraphUtils.getBranchEnd( child2, ref4 );
			return end1.getTimepoint() >= end2.getTimepoint();
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
			graph.releaseRef( ref3 );
			graph.releaseRef( ref4 );
		}
	}
}
