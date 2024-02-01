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
package org.mastodon.mamut.tomancak.merging;

import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.merging.MatchingGraph.MatchingVertexPool;
import org.mastodon.pool.ByteMappedElement;

public class MatchingVertex extends AbstractVertex< MatchingVertex, MatchingEdge, MatchingVertexPool, ByteMappedElement >
{
	MatchingVertex( final MatchingVertexPool pool )
	{
		super( pool );
	}

	public MatchingVertex init( final int graphId, final int spotId )
	{
		pool.graphId.set( this, graphId );
		pool.spotId.set( this, spotId );
		return this;
	}

	int graphId()
	{
		return pool.graphId.get( this );
	}

	int spotId()
	{
		return pool.spotId.get( this );
	}

	public Spot getSpot()
	{
		return getSpot( spotRef() );
	}

	public Spot getSpot( final Spot ref )
	{
		return getModelGraph().getGraphIdBimap().getVertex( spotId(), ref );
	}

	private ModelGraph getModelGraph()
	{
		return pool.modelGraphs.get( graphId() );
	}

	private Spot spotRef()
	{
		return pool.modelGraphs.get( 0 ).vertexRef();
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "{" );
		sb.append( graphId() );
		sb.append( ", " ).append( getSpot().getLabel() );
		sb.append( '}' );
		return sb.toString();
	}
}
