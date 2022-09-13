package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Returns true, if and only is, the first child of the given spot is closer
 * to the "center landmark" than the second child.
 */
public class InternExternOrder implements Predicate<Spot>
{
	private final ModelGraph graph;

	private final List<double[]> centerPositions;

	public InternExternOrder( ModelGraph graph, Collection<Spot> centerLandmarks )
	{
		this.graph = graph;
		int numberOfTimePoints = SortTreeUtils.getNumberOfTimePoints( graph );
		this.centerPositions = SortTreeUtils.calculateAndInterpolateAveragePosition(
				numberOfTimePoints, centerLandmarks );
	}

	@Override
	public boolean test( Spot spot )
	{
		if(spot.outgoingEdges().size() != 2)
			return true;
		double[] devisionDirection = SortTreeUtils.directionOfCellDevision( graph, spot );
		double[] centerPosition = centerPositions.get( spot.getTimepoint() );
		double[] centerDirection = SortTreeUtils.subtract( spot.positionAsDoubleArray(), centerPosition );
		return SortTreeUtils.scalarProduct( devisionDirection, centerDirection ) > 0;
	}
}
