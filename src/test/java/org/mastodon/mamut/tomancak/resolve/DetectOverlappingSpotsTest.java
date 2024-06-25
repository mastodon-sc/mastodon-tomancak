package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TagHelper;

public class DetectOverlappingSpotsTest
{
	@Test
	public void test()
	{
		final Model model = new Model();
		addShortTack( model, 1, 2, 3, 1.1, 2.1, 3.1 );
		addShortTack( model, 5, 3, 4, 5.1, 3.1, 4.1 );
		addShortTack( model, 5, 3.2, 4, 5.1, 3.2, 4.1 );
		DetectOverlappingSpots.run( model, "Conflicting Spots", 0.4 );
		assertEquals( 2, new TagHelper( model, "Conflicting Spots", "Conflict 0 (a)" ).getTaggedSpots().size() );
		assertEquals( 2, new TagHelper( model, "Conflicting Spots", "Conflict 0 (b)" ).getTaggedSpots().size() );
	}

	private void addShortTack( final Model model, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		ModelGraph graph = model.getGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { x1, y1, z1 }, 1 );
		final Spot b = graph.addVertex().init( 1, new double[] { x1, y1, z1 }, 1 );
		graph.addEdge( a, b ).init();
	}

	@Test
	public void testGetLetters()
	{
		assertEquals( "a", DetectOverlappingSpots.getLetters( 0 ) );
		assertEquals( "b", DetectOverlappingSpots.getLetters( 1 ) );
		assertEquals( "z", DetectOverlappingSpots.getLetters( 25 ) );
		assertEquals( "aa", DetectOverlappingSpots.getLetters( 26 ) );
		assertEquals( "az", DetectOverlappingSpots.getLetters( 51 ) );
		assertEquals( "ba", DetectOverlappingSpots.getLetters( 52 ) );
		assertEquals( "bz", DetectOverlappingSpots.getLetters( 77 ) );
		assertEquals( "ca", DetectOverlappingSpots.getLetters( 78 ) );
		assertEquals( "zz", DetectOverlappingSpots.getLetters( 701 ) );
		assertEquals( "aaa", DetectOverlappingSpots.getLetters( 702 ) );
		assertEquals( "aaz", DetectOverlappingSpots.getLetters( 727 ) );
	}
}
