package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

/**
 * Tests for {@link RemoveTagComponents}.
 */
public class RemoveTagComponentsTest
{
	@Test
	public void testRun()
	{
		// Create a graph with the following structure:
		//     a1*
		//    /  \*
		//   a2* a3*
		//    \  /
		//     a4
		//    /  \
		//   a5* a6*
		//    \  /*
		//     a7*
		// Spots and links marked with * are tagged.

		final Model model = new Model();
		final ModelGraph graph = model.getGraph();
		final Spot a1 = addSpot( graph, 1 );
		final Spot a2 = addSpot( graph, 2, a1 );
		final Spot a3 = addSpot( graph, 2, a1 );
		final Spot a4 = addSpot( graph, 3, a2, a3 );
		final Spot a5 = addSpot( graph, 4, a4 );
		final Spot a6 = addSpot( graph, 4, a4 );
		final Spot a7 = addSpot( graph, 5, a5, a6 );

		// set tag
		final TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "tagSet", Collections.singleton( Pair.of( "tag", 0xff0000 ) ) );
		final TagHelper tag = new TagHelper( model, "tagSet", "tag" );
		for ( final Spot spot : Arrays.asList( a1, a2, a3, a5, a6, a7 ) )
			tag.tagSpot( spot );
		tag.tagLink( graph.getEdge( a1, a3 ) );
		tag.tagLink( graph.getEdge( a6, a7 ) );

		// process
		RemoveTagComponents.run( model, tagSet, Collections.singleton( a5 ) );

		// test
		assertEquals( 3, tag.getTaggedSpots().size() );
		assertTrue( tag.getTaggedSpots().containsAll( Arrays.asList( a1, a2, a3 ) ) );
		assertEquals( 1, tag.getTaggedLinks().size() );
		assertTrue( tag.getTaggedLinks().contains( graph.getEdge( a1, a3 ) ) );
	}

	private Spot addSpot( final ModelGraph graph, final int timepoint, final Spot... parents )
	{
		final Spot spot = graph.addVertex().init( timepoint, new double[] { 0, 0, 0 }, 1 );
		for ( final Spot parent : parents )
			graph.addEdge( parent, spot ).init();
		return spot;
	}
}
