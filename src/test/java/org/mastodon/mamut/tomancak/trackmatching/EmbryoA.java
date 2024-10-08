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
package org.mastodon.mamut.tomancak.trackmatching;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Example data for testing {@link SpatialTrackMatchingAlgorithm} and {@link SpatialTrackMatchingUtils}.
 * <p>
 * The graph {@link #graph} is a tree with three lineages, A, B and C. Each
 * lineage divides once.
 * <pre>
 *     A         B         C
 *     |         |        / \
 *     A~1       B~1     C1 C2
 *    / \        |
 *   A1 A2       B~2
 *              / \
 *             B1 B2
 * </pre>
 */
class EmbryoA
{

	final Model model = new Model();

	final ModelGraph graph = model.getGraph();

	final Spot a, aEnd, b, bEnd, c, a1, a2, b1, b2, c1, c2;

	EmbryoA()
	{
		this( 0 );
	}

	EmbryoA( int startTime )
	{
		a = addSpot( graph, "A", startTime, 2, 2, 0 );
		aEnd = addBranch( graph, a, 2 );
		a1 = addSpot( graph, "A1", aEnd, 2, 1, 0 );
		a2 = addSpot( graph, "A2", aEnd, 2, 3, 0 );
		b = addSpot( graph, "B", startTime, 4, 2, 0 );
		bEnd = addBranch( graph, b, 3 );
		b1 = addSpot( graph, "B1", bEnd, 4, 1, 0 );
		b2 = addSpot( graph, "B2", bEnd, 4, 3, 0 );
		c = addSpot( graph, "C", startTime, 4, 4, 0 );
		c1 = addSpot( graph, "C1", c, 4, 4, 0 );
		c2 = addSpot( graph, "C2", c, 4, 5, 0 );
	}

	// helper methods

	private static Spot addSpot( ModelGraph graph, String label, int time, double... position )
	{
		Spot spot = graph.addVertex().init( time, position, 1 );
		spot.setLabel( label );
		return spot;
	}

	static Spot addSpot( ModelGraph graph, String label, Spot parent, double... position )
	{
		int time = parent == null ? 2 : parent.getTimepoint() + 1;
		Spot spot = addSpot( graph, label, time, position );
		if ( parent != null )
			graph.addEdge( parent, spot );
		return spot;
	}

	static Spot addBranch( ModelGraph graph, Spot branchStart, int length )
	{
		String label = branchStart.getLabel();
		double[] position = { branchStart.getDoublePosition( 0 ), branchStart.getDoublePosition( 1 ), branchStart.getDoublePosition( 2 ) };
		Spot s = branchStart;
		for ( int i = 1; i < length; i++ )
			s = addSpot( graph, label + "~" + i, s, position );
		return s;
	}
}
