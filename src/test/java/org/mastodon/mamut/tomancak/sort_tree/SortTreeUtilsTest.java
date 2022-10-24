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

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SortTreeUtilsTest
{
	@Test
	public void testDirectionOfCellDevision() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot spot = graph.addVertex().init( 0, array(2, 2, 2), 0.5 );
		Spot a = graph.addVertex().init( 1, array( 1, 2, 2), 0.5 );
		Spot b = graph.addVertex().init( 1, array( 3, 2, 2), 0.5 );
		graph.addEdge( spot, a ).init();
		graph.addEdge( spot, b ).init();
		// process
		double[] direction = SortTreeUtils.directionOfCellDevision( graph, spot );
		// test
		assertArrayEquals(array(2, 0, 0), direction, 0.0);
	}

	@Test
	public void testDirectionOfCellDevision2() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot spot = graph.addVertex().init( 0, array(2, 2, 2), 0.5 );
		Spot a1 = graph.addVertex().init( 1, array( 0, 2, 2), 0.5 );
		Spot a2 = graph.addVertex().init( 2, array( 5, 2, 2), 0.5 );
		Spot a3 = graph.addVertex().init( 3, array( 1, 2, 2), 0.5 );
		Spot a4 = graph.addVertex().init( 4, array( 1, 2, 2), 0.5 );
		Spot b1 = graph.addVertex().init( 1, array( 0, 2, 2), 0.5 );
		Spot b2 = graph.addVertex().init( 2, array( 5, 2, 2), 0.5 );
		Spot b3_1 = graph.addVertex().init( 3, array( 10, 2, 2), 0.5 );
		Spot b3_2 = graph.addVertex().init( 3, array( 12, 2, 2), 0.5 );
		graph.addEdge( spot, a1 ).init();
		graph.addEdge( a1, a2 ).init();
		graph.addEdge( a2, a3 ).init();
		graph.addEdge( a3, a4 ).init();
		graph.addEdge( spot, b1 ).init();
		graph.addEdge( b1, b2 ).init();
		graph.addEdge( b2, b3_1 ).init();
		graph.addEdge( b2, b3_2 ).init();
		// process
		double[] direction = SortTreeUtils.directionOfCellDevision( graph, spot );
		// test
		assertArrayEquals(array(0.5, 0, 0), direction, 0.0);

	}


	@Test
	public void testTagAverageLocation() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, array( 1, 2, 3 ), 0.5 );
		Spot b = graph.addVertex().init( 0, array(3, 4, 5), 0.5 );
		// process
		double[] averagePosition = SortTreeUtils.calculateAndInterpolateAveragePosition( 1, Arrays.asList(a, b)).get(0);
		// test
		assertArrayEquals( array(2,3,4), averagePosition, 0.0 );
	}

	@Test
	public void testInterpolatedLocation() {
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 1, array( 1, 2, 3 ), 0.5 );
		Spot b = graph.addVertex().init( 5, array(5, 6, 7), 0.5 );
		List<double[]> positions = SortTreeUtils.calculateAndInterpolateAveragePosition( 7, Arrays.asList( a, b ));
		assertEquals(7, positions.size());
		assertArrayEquals( array(1,2,3), positions.get(0), 0.0 );
		assertArrayEquals( array(1,2,3), positions.get(1), 0.0 );
		assertArrayEquals( array(2,3,4), positions.get(2), 0.0 );
		assertArrayEquals( array(3,4,5), positions.get(3), 0.0 );
		assertArrayEquals( array(4,5,6), positions.get(4), 0.0 );
		assertArrayEquals( array(5,6,7), positions.get(5), 0.0 );
		assertArrayEquals( array(5,6,7), positions.get(6), 0.0 );
	}

	private double[] array( double... values )
	{
		return values;
	}
}
