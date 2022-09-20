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

import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

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

	public static void sortLeftRightAnchors( Model model, Collection<Spot> vertices, Collection<Spot> leftAnchors, Collection<Spot> rightAnchors )
	{
		sort( model, vertices, new LeftRightOrder( model.getGraph(), leftAnchors, rightAnchors ) );
	}

	public static void sortExternIntern( Model model, Collection<Spot> vertices, Collection<Spot> centerSpots )
	{
		sort( model, vertices, new ExternInternOrder( model.getGraph(), centerSpots ) );
	}

	public static void sort( Model model, Collection<Spot> vertices, Predicate<Spot> order )
	{
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			RefList<Spot> toBeFlipped = findSpotsToBeFlipped( graph, vertices, order );
			FlipDescendants.flipDescendants( model, toBeFlipped );
		}
		finally
		{
			lock.unlock();
		}
	}

	private static RefList<Spot> findSpotsToBeFlipped( ModelGraph graph, Collection<Spot> vertices, Predicate<Spot> correctOrder )
	{
		RefList<Spot> toBeFlipped = new RefArrayList<>( graph.vertices().getRefPool() );
		for(Spot spot : vertices )
		{
			boolean needsToBeFlipped = spot.outgoingEdges().size() == 2
					&& !correctOrder.test( spot );
			if ( needsToBeFlipped )
				toBeFlipped.add( spot );
		}
		return toBeFlipped;
	}
}
