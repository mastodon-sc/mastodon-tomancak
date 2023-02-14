package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.PoolCollectionWrapper;

public class LineageTreeUtils
{
	/**
	 * @return the set of root nodes of the give graph.
	 */
	public static RefSet< Spot > getRoots( ModelGraph graph )
	{
		PoolCollectionWrapper< Spot > vertices = graph.vertices();
		RefSetImp< Spot > roots = new RefSetImp<>( vertices.getRefPool() );
		for ( Spot spot : vertices )
			if ( spot.incomingEdges().isEmpty() )
				roots.add( spot );
		return roots;
	}

	/**
	 * The given spot belongs to a branch. This method returns the last
	 * spot of this branch.
	 */
	public static Spot getBranchEnd( final Spot spot, final Spot ref )
	{
		Spot s = ref;
		s.refTo( spot );
		while ( s.outgoingEdges().size() == 1 )
			s = s.outgoingEdges().get( 0 ).getTarget( s );
		return s;
	}

	/**
	 * @return true if the given spot is part of a branch that divides.
	 */
	public static boolean doesBranchDivide( ModelGraph graph, final Spot spot )
	{
		Spot ref = graph.vertexRef();
		try
		{
			Spot branchEnd = getBranchEnd( spot, ref );
			return branchEnd.outgoingEdges().size() > 1;
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}
}
