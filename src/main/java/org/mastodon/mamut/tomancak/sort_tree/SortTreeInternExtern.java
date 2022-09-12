package org.mastodon.mamut.tomancak.sort_tree;

import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SortTreeInternExtern
{
	public static void sort( Model model, Collection<Spot> vertices, Collection<Spot> centerSpots )
	{
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			int numberOfTimePoints = SortTreeUtils.getNumberOfTimePoints( graph );
			List<double[]> centerPosition = SortTreeUtils.calculateAndInterpolateAveragePosition( numberOfTimePoints, centerSpots );
			RefList<Spot> toBeFlipped = findSpotsToBeFlipped( graph, vertices, centerPosition );
			FlipDescendants.flipDescendants( model, toBeFlipped );
		}
		finally
		{
			lock.unlock();
		}
	}

	private static RefList<Spot> findSpotsToBeFlipped( ModelGraph graph, Collection<Spot> vertices, List<double[]> centerPosition )
	{
		RefList<Spot> toBeFlipped = new RefArrayList<>( graph.vertices().getRefPool() );
		for(Spot spot : vertices )
			if ( needsToBeFlipped( graph, spot, centerPosition.get( spot.getTimepoint() ) ) )
				toBeFlipped.add( spot );
		return toBeFlipped;
	}

	private static boolean needsToBeFlipped( ModelGraph graph, Spot spot, double[] center )
	{
		if(spot.outgoingEdges().size() != 2)
			return false;
		double[] devisionDirection = SortTreeUtils.directionOfCellDevision( graph, spot );
		double[] centerDirection = SortTreeUtils.subtract( spot.positionAsDoubleArray(), center );
		return SortTreeUtils.scalarProduct( devisionDirection, centerDirection ) < 0;
	}
}
