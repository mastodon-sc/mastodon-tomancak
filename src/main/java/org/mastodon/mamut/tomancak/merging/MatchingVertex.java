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
