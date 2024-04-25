package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mastodon.mamut.tomancak.resolve.HellingerDistanceTest.diagonal;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

public class AverageSpotsTest
{
	@Test
	public void testRun()
	{
		final Model model = new Model();
		final List< Pair< String, Integer > > tagsAndColors = Arrays.asList( Pair.of( "a", Color.red.getRGB() ), Pair.of( "b", Color.green.getRGB() ) );
		TagSetUtils.addNewTagSetToModel( model, "tagSet", tagsAndColors );
		final TagHelper tagA = new TagHelper( model, "tagSet", "a" );
		final TagHelper tagB = new TagHelper( model, "tagSet", "b" );
		final ModelGraph graph = model.getGraph();

		final Spot a1 = graph.addVertex().init( 1, new double[] { 1, 1, 1 }, diagonal( 1, 3, 4 ) );
		final Spot a2 = graph.addVertex().init( 2, new double[] { 2, 1, 1 }, diagonal( 2, 3, 4 ) );
		final Spot a3 = graph.addVertex().init( 3, new double[] { 3, 1, 1 }, diagonal( 3, 3, 4 ) );
		final Spot a4 = graph.addVertex().init( 4, new double[] { 4, 1, 1 }, diagonal( 4, 3, 4 ) );

		tagA.tagLink( graph.addEdge( a1, a2 ).init() );
		tagA.tagLink( graph.addEdge( a2, a3 ).init() );
		tagA.tagLink( graph.addEdge( a3, a4 ).init() );

		final Spot b1 = graph.addVertex().init( 1, new double[] { 1, 2, 1 }, diagonal( 1, 4, 4 ) );
		final Spot b2 = graph.addVertex().init( 2, new double[] { 2, 2, 1 }, diagonal( 2, 4, 4 ) );
		final Spot b3 = graph.addVertex().init( 3, new double[] { 3, 2, 1 }, diagonal( 3, 4, 4 ) );
		final Spot b4 = graph.addVertex().init( 4, new double[] { 4, 2, 1 }, diagonal( 4, 4, 4 ) );

		tagB.tagLink( graph.addEdge( b1, b2 ).init() );
		tagB.tagLink( graph.addEdge( b2, b3 ).init() );
		tagB.tagLink( graph.addEdge( b3, b4 ).init() );

		AverageSpots.run( model, Arrays.asList( a2, a3, b2, b3 ), a2 );

		assertEquals( 6, graph.vertices().size() );
		assertArrayEquals( new double[] { 2, 1.5, 1 }, a2.positionAsDoubleArray(), 1e-10 );
		assertArrayEquals( new double[] { 3, 1.5, 1 }, a3.positionAsDoubleArray(), 1e-10 );
		final double[][] cov = new double[ 3 ][ 3 ];
		a2.getCovariance( cov );
		assertArrayEquals( diagonal( 2, 3.5, 4 ), cov );
		a3.getCovariance( cov );
		assertArrayEquals( diagonal( 3, 3.5, 4 ), cov );

		assertTrue( tagB.isTagged( graph.getEdge( b1, a2 ) ) );
		assertTrue( tagB.isTagged( graph.getEdge( a3, b4 ) ) );
	}
}
