/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SortTreeTest
{
	@Test
	public void testFlip() {
		// setup
		Model model = new Model();
		ModelGraph graph = model.getGraph();
		Spot spot = graph.addVertex().init( 0, array(2, 2, 2), 0.5 );
		Spot a = graph.addVertex().init( 1, array( 3, 2, 2), 0.5 );
		Spot b = graph.addVertex().init( 1, array( 1, 2, 2), 0.5 );
		graph.addEdge( spot, a ).init();
		graph.addEdge( spot, b ).init();
		Spot left = graph.addVertex().init( 0, array( 0, 0, 0 ), 0.1 );
		Spot right = graph.addVertex().init( 0, array( 1, 0, 0 ), 0.1 );
		// process
		SortTree.sortLeftRightAnchors(model, graph.vertices(), Collections.singleton( left ), Collections.singleton( right ) );
		// test
		assertEquals(b, spot.outgoingEdges().get(0).getTarget());
		assertEquals(a, spot.outgoingEdges().get(1).getTarget());
	}

	private double[] array( double... values )
	{
		return values;
	}
}
