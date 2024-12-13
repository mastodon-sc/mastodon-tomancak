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

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TagHelper;

public class CreateConflictTagSetTest
{
	@Test
	public void testRun()
	{
		final Model model = new Model();
		addShortTack( model, 1, 2, 3, 1.1, 2.1, 3.1 );
		addShortTack( model, 5, 3, 4, 5.1, 3.1, 4.1 );
		addShortTack( model, 5, 3.2, 4, 5.1, 3.2, 4.1 );
		CreateConflictTagSet.run( model, "Conflicting Spots", 0.4, null );
		assertEquals( 2, new TagHelper( model, "Conflicting Spots", "Conflict 0 (a)" ).getTaggedSpots().size() );
		assertEquals( 2, new TagHelper( model, "Conflicting Spots", "Conflict 0 (b)" ).getTaggedSpots().size() );
	}

	private void addShortTack( final Model model, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		final ModelGraph graph = model.getGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { x1, y1, z1 }, 1 );
		final Spot b = graph.addVertex().init( 1, new double[] { x2, y2, z2 }, 1 );
		graph.addEdge( a, b ).init();
	}

	@Test
	public void testGetLetters()
	{
		assertEquals( "a", CreateConflictTagSet.getLetters( 0 ) );
		assertEquals( "b", CreateConflictTagSet.getLetters( 1 ) );
		assertEquals( "z", CreateConflictTagSet.getLetters( 25 ) );
		assertEquals( "aa", CreateConflictTagSet.getLetters( 26 ) );
		assertEquals( "az", CreateConflictTagSet.getLetters( 51 ) );
		assertEquals( "ba", CreateConflictTagSet.getLetters( 52 ) );
		assertEquals( "bz", CreateConflictTagSet.getLetters( 77 ) );
		assertEquals( "ca", CreateConflictTagSet.getLetters( 78 ) );
		assertEquals( "zz", CreateConflictTagSet.getLetters( 701 ) );
		assertEquals( "aaa", CreateConflictTagSet.getLetters( 702 ) );
		assertEquals( "aaz", CreateConflictTagSet.getLetters( 727 ) );
	}
}
