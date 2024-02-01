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
package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.mamut.model.Spot;

/**
 * Example data for testing {@link LineageRegistrationAlgorithm} and {@link LineageRegistrationUtils}.
 * The graph and coordinates are the similar to {@link EmbryoB}, but the graph in this class has
 * additional spots abc, bc and beforeA, that are added to the graph before the first spots of {@link EmbryoB}.
 * <p>
 * {@link EmbryoBSingleCellStage#graph} has therefore only one root node: abc.
 *
 */
class EmbryoBSingleCellStage extends EmbryoB
{

	Spot abc;

	Spot bc;

	Spot beforeA;

	EmbryoBSingleCellStage()
	{
		super( 2 );
		abc = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		bc = graph.addVertex().init( 1, new double[] { 1, 0, 0 }, 1 );
		beforeA = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 1 );
		beforeA.setLabel( "A" );
		graph.addEdge( abc, bc );
		graph.addEdge( abc, beforeA );
		graph.addEdge( beforeA, a );
		graph.addEdge( bc, b );
		graph.addEdge( bc, c );
	}

}
