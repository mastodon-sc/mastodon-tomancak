package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class LineageRegistrationAlgorithmTest
{

	/**
	 * Test if registering two very similar lineage graphs with only one
	 * division flipped works.
	 */
	@Test
	public void testRun() {
		// NB: The graphs need to have at least 3 dividing lineages.
		// Only the root nodes of the dividing lineages are used
		// to calculate the affine transform between the two "embryos".
		
		Model embryoA = new Model();
		ModelGraph graphA = embryoA.getGraph();
		Spot sA = addSpot( graphA, null, "A", 2, 2, 0);
		Spot sA1 = addSpot( graphA, sA, "A1", 2, 1, 0);
		Spot sA2 = addSpot( graphA, sA, "A2", 2, 3, 0);
		Spot sB = addSpot( graphA, null, "B", 4, 2, 0);
		Spot sB1 = addSpot( graphA, sB, "B1", 4, 1, 0);
		Spot sB2 = addSpot( graphA, sB, "B2", 4, 3, 0);
		Spot sC = addSpot( graphA, null, "C", 4, 4, 0);
		Spot sC1 = addSpot( graphA, sC, "C", 4, 4, 0);
		Spot sC2 = addSpot( graphA, sC, "C", 4, 5, 0);

		// "graphB" is very similar to "graphA",but has the following differences:
		// - 1. y and z coordinates are flipped, this simulates an 90 degree rotation around the X axis.
		// - 2. coordinates of nodes "B1" and "B2" are exchanged compared to graphA.
		Model embryoB = new Model();
		ModelGraph graphB = embryoB.getGraph();
		Spot tA = addSpot( graphB, null, "A", 2, 0, 2);
		Spot tA1 = addSpot( graphB, tA, "A1", 2, 0, 1);
		Spot tA2 = addSpot( graphB, tA, "A2", 2, 0, 3);
		Spot tB = addSpot( graphB, null, "B", 4, 0, 2);
		Spot tB1 = addSpot( graphB, tB, "B1", 4, 0, 3);
		Spot tB2 = addSpot( graphB, tB, "B2", 4, 0, 1);
		Spot tC = addSpot( graphB, null, "C", 4, 0, 4);
		Spot tC1 = addSpot( graphB, tC, "C", 4, 0, 4);
		Spot tC2 = addSpot( graphB, tC, "C", 4, 0, 5);
		
		assertEquals( tA1, firstChild(tA) );
		assertEquals( tB1, firstChild(tB) );
		assertEquals( tC1, firstChild(tC) );
		
		LineageRegistrationAlgorithm.run( embryoA, embryoB );

		// Only the child nodes of tB are expected to be flipped.
		assertEquals( tA1, firstChild(tA) );
		assertEquals( tB2, firstChild(tB) );
		assertEquals( tC1, firstChild(tC) );
	}

	private Spot firstChild( Spot tA )
	{
		return tA.outgoingEdges().get(0).getTarget();
	}

	private Spot addSpot( ModelGraph graph, Spot parent, String label, double x, double y, double z )
	{
		int t = parent == null ? 0 : parent.getTimepoint() + 1;
		Spot spot = graph.addVertex().init( t, new double[] { x, y, z }, 1 );
		spot.setLabel( label );
		if( parent != null )
		graph.addEdge( parent, spot );
		return spot;
	}
}
