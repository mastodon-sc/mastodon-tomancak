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

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Methods for sorting the lineage tree in a {@link ModelGraph}.
 * <p>
 * The order of the outgoing edges of all nodes are changed such that
 * the child that is closer to the "left anchor" is first and the child closer
 * to the "right anchor" is second.
 *
 * @author Matthias Arzt
 */
public class SortTree
{

	public static void sort( Model model, Collection<Spot> vertices, Collection<Spot> leftAnchors, Collection<Spot> rightAnchors )
	{
		ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.writeLock().lock();
		try
		{
			int numberOfTimePoints = SortTreeUtils.getNumberOfTimePoints( model.getGraph() );
			// calculate directions
			List<double[]> left = SortTreeUtils.calculateAndInterpolateAveragePosition( numberOfTimePoints, leftAnchors );
			List<double[]> right = SortTreeUtils.calculateAndInterpolateAveragePosition( numberOfTimePoints, rightAnchors );
			List<double[]> directions = SortTreeUtils.subtract( right, left );
			// find vertices to be flipped
			Collection<Spot> toBeFlipped = findSpotsToBeFlipped( model.getGraph(), vertices, directions );
			// flip vertices
			FlipDescendants.flipDescendants( model, toBeFlipped );
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	public static Collection<Spot> findSpotsToBeFlipped( ModelGraph graph, Collection<Spot> selection, List<double[]> sortingDirections )
	{
		RefArrayList<Spot> result = new RefArrayList<>( graph.vertices().getRefPool() );
		for(Spot spot : selection)
			if( isSortingWrong(graph, spot, sortingDirections.get(spot.getTimepoint())) )
				result.add(spot);
		return result;
	}

	private static boolean isSortingWrong( ModelGraph graph, Spot spot, double[] sortingDirection )
	{
		if(spot.outgoingEdges().size() != 2)
			return false;
		double[] divisionDirection = SortTreeUtils.directionOfCellDevision( graph, spot );
		return SortTreeUtils.scalarProduct(sortingDirection, divisionDirection) < 0;
	}

}
