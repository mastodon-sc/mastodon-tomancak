package org.mastodon.mamut.tomancak.merging;

import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.mamut.tomancak.merging.MatchingGraph.MatchingEdgePool;
import org.mastodon.pool.ByteMappedElement;

public class MatchingEdge extends AbstractEdge< MatchingEdge, MatchingVertex, MatchingEdgePool, ByteMappedElement >
{
	MatchingEdge( final MatchingEdgePool pool )
	{
		super( pool );
	}

	public MatchingEdge init( final double distSqu, final double mahalDistSqu )
	{
		pool.distSqu.set( this, distSqu );
		pool.mahalDistSqu.set( this, mahalDistSqu );
		return this;
	}

	public double getDistSqu()
	{
		return pool.distSqu.get( this );
	}

	public void setDistSqu( final double d )
	{
		pool.distSqu.set( this, d );
	}

	public double getMahalDistSqu()
	{
		return pool.mahalDistSqu.get( this );
	}

	public void setMahalDistSqu( final double d )
	{
		pool.mahalDistSqu.set( this, d );
	}
}
