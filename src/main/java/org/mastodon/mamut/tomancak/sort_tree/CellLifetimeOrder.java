/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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

import java.util.Iterator;
import java.util.function.Predicate;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.trackmatching.BranchGraphUtils;

/**
 * A sorting "order" for sorting the lineage tree in a {@link ModelGraph}.
 * Compares the cell lifecycles of the two child cells of a given spot.
 * The child with the longer cell lifecycle is sorted to be the first child.
 * <pre>
 * {@code
 *          ┌─────┴─────┐                ┌─────┴─────┐
 *          │2          │3               │3          │2
 *          │           │                │           │
 *       ┌──┴──┐        │       ==>      │        ┌──┴──┐
 *       │1    │2    ┌──┴──┐          ┌──┴──┐     │2    │1
 *             │     │2    │1         │2    │1    │
 *                   │                │
 *}
 * </pre>
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
