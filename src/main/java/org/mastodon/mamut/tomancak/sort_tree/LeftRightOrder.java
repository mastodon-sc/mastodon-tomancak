package org.mastodon.mamut.tomancak.sort_tree;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class LeftRightOrder implements Predicate<Spot>
{

	private final ModelGraph graph;

	private final List<double[]> directions;

	public LeftRightOrder( ModelGraph graph, Collection<Spot> leftAnchors, Collection<Spot> rightAnchors )
	{
		this.graph = graph;
		int numberOfTimePoints = SortTreeUtils.getNumberOfTimePoints( graph );
		List<double[]> left = SortTreeUtils.calculateAndInterpolateAveragePosition( numberOfTimePoints, leftAnchors );
		List<double[]> right = SortTreeUtils.calculateAndInterpolateAveragePosition( numberOfTimePoints, rightAnchors );
		this.directions = SortTreeUtils.subtract( right, left );
	}

	@Override
	public boolean test( Spot spot )
	{
		if (spot.outgoingEdges().size() != 2)
			return true;
		double[] divisionDirection = SortTreeUtils.directionOfCellDevision( graph, spot );
		double[] sortingDirection = directions.get( spot.getTimepoint() );
		return SortTreeUtils.scalarProduct( sortingDirection, divisionDirection) >= 0;
	}
}
