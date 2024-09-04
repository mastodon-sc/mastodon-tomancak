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
