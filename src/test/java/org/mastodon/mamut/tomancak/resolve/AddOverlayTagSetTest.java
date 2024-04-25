package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TagHelper;

public class AddOverlayTagSetTest
{
	@Test
	public void test()
	{
		final Model model = new Model();
		addShortTack( model, 1, 2, 3, 1.1, 2.1, 3.1 );
		addShortTack( model, 5, 3, 4, 5.1, 3.1, 4.1 );
		addShortTack( model, 5, 3.2, 4, 5.1, 3.2, 4.1 );
		AddOverlayTagSet.run( model );
		assertEquals( 2, new TagHelper( model, "Overlapping Spots", "Conflict 0 A" ).getTaggedSpots().size() );
		assertEquals( 2, new TagHelper( model, "Overlapping Spots", "Conflict 0 B" ).getTaggedSpots().size() );
	}

	private void addShortTack( final Model model, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		ModelGraph graph = model.getGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { x1, y1, z1 }, 1 );
		final Spot b = graph.addVertex().init( 1, new double[] { x1, y1, z1 }, 1 );
		graph.addEdge( a, b ).init();
	}
}
