/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
