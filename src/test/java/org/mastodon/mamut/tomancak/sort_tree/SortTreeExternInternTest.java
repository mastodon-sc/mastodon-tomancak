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

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SortTreeExternInternTest
{
	@Test
	public void test() {
		Model model = new Model();
		// example graph
		ModelGraph graph = model.getGraph();
		Spot a = graph.addVertex().init( 0, array( 3, 5, 5), 1 );
		Spot aIntern = graph.addVertex().init( 1, array( 4, 5, 5 ), 1 );
		Spot aExtern = graph.addVertex().init( 1, array( 2, 5, 5 ), 1 );
		Spot b = graph.addVertex().init( 0, array( 7, 5, 5), 1 );
		Spot bIntern = graph.addVertex().init( 1, array( 6, 5, 5 ), 1 );
		Spot bExtern = graph.addVertex().init( 1, array( 8, 5, 5 ), 1 );
		graph.addEdge( a, aExtern ).init();
		graph.addEdge( a, aIntern ).init();
		graph.addEdge( b, bIntern ).init();
		graph.addEdge( b, bExtern ).init();
		// add center marker
		Spot center = graph.addVertex().init( 0, array(5,5,5), 1 );
		// sort
		SortTree.sortExternIntern(model, graph.vertices(), Collections.singleton(center));
		// assert
		assertEquals(aExtern, a.outgoingEdges().iterator().next().getTarget());
		assertEquals(bExtern, b.outgoingEdges().iterator().next().getTarget());
	}

	private double[] array( double... values )
	{
		return values;
	}
}
