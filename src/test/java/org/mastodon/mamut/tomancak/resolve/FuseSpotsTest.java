package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mastodon.mamut.tomancak.resolve.HellingerDistanceTest.diagonal;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

public class FuseSpotsTest
{

	private Model model;

	private ModelGraph graph;

	private TagHelper tagA;

	private TagHelper tagB;

	private Spot a1;

	private Spot a2;

	private Spot a3;

	private Spot a4;

	private Spot b1;

	private Spot b2;

	private Spot b3;

	private Spot b4;

	/**
	 * Set up a graph with two tracks, each with 4 spots. Fuse the middle spots of both tracks:
	 **/
	@Before
	public void before()
	{
		model = new Model();
		graph = model.getGraph();
		final List< Pair< String, Integer > > tagsAndColors = Arrays.asList( Pair.of( "a", Color.red.getRGB() ), Pair.of( "b", Color.green.getRGB() ) );
		TagSetUtils.addNewTagSetToModel( model, "tagSet", tagsAndColors );
		tagA = new TagHelper( model, "tagSet", "a" );
		tagB = new TagHelper( model, "tagSet", "b" );

		a1 = graph.addVertex().init( 1, new double[] { 1, 1, 1 }, diagonal( 1, 3, 4 ) );
		a2 = graph.addVertex().init( 2, new double[] { 2, 1, 1 }, diagonal( 2, 3, 4 ) );
		a3 = graph.addVertex().init( 3, new double[] { 3, 1, 1 }, diagonal( 3, 3, 4 ) );
		a4 = graph.addVertex().init( 4, new double[] { 4, 1, 1 }, diagonal( 4, 3, 4 ) );

		tagA.tagLink( graph.addEdge( a1, a2 ).init() );
		tagA.tagLink( graph.addEdge( a2, a3 ).init() );
		tagA.tagLink( graph.addEdge( a3, a4 ).init() );

		b1 = graph.addVertex().init( 1, new double[] { 1, 2, 1 }, diagonal( 1, 4, 4 ) );
		b2 = graph.addVertex().init( 2, new double[] { 2, 2, 1 }, diagonal( 2, 4, 4 ) );
		b3 = graph.addVertex().init( 3, new double[] { 3, 2, 1 }, diagonal( 3, 4, 4 ) );
		b4 = graph.addVertex().init( 4, new double[] { 4, 2, 1 }, diagonal( 4, 4, 4 ) );

		tagB.tagLink( graph.addEdge( b1, b2 ).init() );
		tagB.tagLink( graph.addEdge( b2, b3 ).init() );
		tagB.tagLink( graph.addEdge( b3, b4 ).init() );
	}

	/**
	 * Test merge operation:
	 * <pre>
	 *    a1     b1             a1    b1
	 *    |      |                \  /
	 *    a2     b2                a2
	 *    |      |      ===>       |
	 *    a3     b3                a3
	 *    |      | 	              /  \
	 *    a4     b4             a4    b4
	 * </pre>
	 */
	@Test
	public void testRun()
	{
		FuseSpots.run( model, Arrays.asList( a2, a3, b2, b3 ), a2 );

		assertEquals( 6, graph.vertices().size() );
		assertEquals( 5, graph.edges().size() );
		assertArrayEquals( new double[] { 2, 1.5, 1 }, a2.positionAsDoubleArray(), 1e-10 );
		assertArrayEquals( new double[] { 3, 1.5, 1 }, a3.positionAsDoubleArray(), 1e-10 );
		final double[][] cov = new double[ 3 ][ 3 ];
		a2.getCovariance( cov );
		assertArrayEquals( diagonal( 2, 3.5, 4 ), cov );
		a3.getCovariance( cov );
		assertArrayEquals( diagonal( 3, 3.5, 4 ), cov );

		assertTrue( tagB.isTagged( graph.getEdge( b1, a2 ) ) );
		assertTrue( tagA.isTagged( graph.getEdge( a2, a3 ) ) );
		assertTrue( tagB.isTagged( graph.getEdge( a3, b4 ) ) );
	}

	/**
	 * Same test as {@link #testRun()} but with no focused spot.
	 */
	@Test
	public void testRunNoFocusedSpot()
	{
		FuseSpots.run( model, Arrays.asList( a2, a3, b2, b3 ), null );
		assertEquals( 6, graph.vertices().size() );
		assertEquals( 5, graph.edges().size() );
	}

	@Test
	public void testNoDoubleEdges()
	{
		FuseSpots.run( model, Arrays.asList( a2, b2 ), a2 );
		// After the fusion, the graph locks like this:
		//    a1     b1             a1    b1
		//    |      |                \  /
		//    a2     b2                a2
		//    |      |      ===>      /  \
		//    a3     b3             a3    b3
		//    |      |               |    |
		//    a4     b4             a4    b4
		// Fusing a3 and b3 means that two edges are fused into one.
		// The tag of the remaining should be tagA because it's the
		// tag of the edge a2->a3, and a3 is the focused spot.
		FuseSpots.run( model, Arrays.asList( b3, a3 ), a3 );
		assertEquals( 6, graph.vertices().size() );
		assertEquals( 5, graph.edges().size() );
		assertTrue( tagA.isTagged( graph.getEdge( a2, a3 ) ) );
	}

	@Test
	public void testNonParallelBranches()
	{
		assertThrows( Exception.class, () -> FuseSpots.run( model, Arrays.asList( a2, a3, b2 ), a2 ) );
		assertThrows( Exception.class, () -> FuseSpots.run( model, Arrays.asList( a2, a4, b2, b4 ), a2 ) );
		assertThrows( Exception.class, () -> FuseSpots.run( model, Arrays.asList( a2, b3 ), a2 ) );
	}

	@Test
	public void testNonParallelBranches2()
	{
		graph.remove( graph.getEdge( b3, b4 ) );
		graph.addEdge( a2, b4 ).init();
		// a2 has two outgoing edges, so a2->a3 is no longer a simple branch.
		assertThrows( Exception.class, () -> FuseSpots.run( model, Arrays.asList( a2, a3, b2, b3 ), a2 ) );
	}
}
