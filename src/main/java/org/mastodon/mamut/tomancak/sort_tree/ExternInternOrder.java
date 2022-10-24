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
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Returns true, if and only if, the first child of the given spot is further
 * away from the "center landmark" than the second child.
 */
public class ExternInternOrder implements Predicate<Spot>
{
	private final ModelGraph graph;

	private final List<double[]> centerPositions;

	public ExternInternOrder( ModelGraph graph, Collection<Spot> centerLandmarks )
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
		return SortTreeUtils.scalarProduct( devisionDirection, centerDirection ) < 0;
	}
}
