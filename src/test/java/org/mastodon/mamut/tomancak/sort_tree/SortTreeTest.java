package org.mastodon.mamut.tomancak.sort_tree;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SortTreeTest
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
		double[] direction = SortTree.directionOfCellDevision( graph, spot );
		// test
		assertArrayEquals(array(2, 0, 0), direction, 0.0);
	}

	@Test
	public void testTagAverageLocation() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, array( 1, 2, 3 ), 0.5 );
		Spot b = graph.addVertex().init( 0, array(3, 4, 5), 0.5 );
		// process
		double[] averagePosition = SortTree.calculateAndInterpolateAveragePosition( 1, Arrays.asList(a, b)).get(0);
		// test
		assertArrayEquals( array(2,3,4), averagePosition, 0.0 );
	}

	@Test
	public void testInterpolatedLocation() {
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 1, array( 1, 2, 3 ), 0.5 );
		Spot b = graph.addVertex().init( 5, array(5, 6, 7), 0.5 );
		List<double[]> positions = SortTree.calculateAndInterpolateAveragePosition( 7, Arrays.asList( a, b ));
		assertEquals(7, positions.size());
		assertArrayEquals( array(1,2,3), positions.get(0), 0.0 );
		assertArrayEquals( array(1,2,3), positions.get(1), 0.0 );
		assertArrayEquals( array(2,3,4), positions.get(2), 0.0 );
		assertArrayEquals( array(3,4,5), positions.get(3), 0.0 );
		assertArrayEquals( array(4,5,6), positions.get(4), 0.0 );
		assertArrayEquals( array(5,6,7), positions.get(5), 0.0 );
		assertArrayEquals( array(5,6,7), positions.get(6), 0.0 );
	}

	@Test
	public void testSortTree() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot spot = graph.addVertex().init( 0, array(2, 2, 2), 0.5 );
		Spot a = graph.addVertex().init( 1, array( 1, 2, 2), 0.5 );
		Spot b = graph.addVertex().init( 1, array( 3, 2, 2), 0.5 );
		graph.addEdge( spot, a ).init();
		graph.addEdge( spot, b ).init();
		Spot a1 =  graph.addVertex().init( 2, array( 1.5, 2, 2 ), 0.5 );
		Spot a2 =  graph.addVertex().init( 2, array( 0.5, 2, 2 ), 0.5 );
		graph.addEdge( a, a1 ).init();
		graph.addEdge( a, a2 ).init();
		double[] direction = array(-1, 0, 0);
		// process
		Collection<Spot> spots = SortTree.findSpotsToBeFlipped( graph, graph.vertices(), Collections.nCopies( 3, direction ) );
		// test
		assertEquals( Collections.singletonList( spot ), spots );
	}

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
		SortTree.sort(model, graph.vertices(), Collections.singleton( left ), Collections.singleton( right ) );
		// test
		assertEquals(b, spot.outgoingEdges().get(0).getTarget());
		assertEquals(a, spot.outgoingEdges().get(1).getTarget());
	}

	private double[] array( double... values )
	{
		return values;
	}
}
