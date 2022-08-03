/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.spots;

import static net.imglib2.util.LinAlgHelpers.rows;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import net.imglib2.util.LinAlgHelpers;

public class InterpolateMissingSpots
{
	public static void interpolate( final Model model )
	{
		new InterpolateMissingSpots( model ).interpolate();
	}

	private final Model model;
	private final ModelGraph graph;

	private final Spot vref1;
	private final Spot vref2;
	private final Spot vref3;

	private final Link eref1;

	private double[] pos0 = new double[ 3 ];
	private double[] pos1 = new double[ 3 ];
	private double[] pos = new double[ 3 ];

	private double[][] cov0 = new double[ 3 ][ 3 ];
	private double[][] cov = new double[ 3 ][ 3 ];

	private InterpolateMissingSpots( final Model model )
	{
		this.model = model;
		this.graph = model.getGraph();
		vref1 = graph.vertexRef();
		vref2 = graph.vertexRef();
		vref3 = graph.vertexRef();
		eref1 = graph.edgeRef();
	}

	private void interpolate()
	{
		final ReentrantReadWriteLock lock = graph.getLock();
		lock.writeLock().lock();
		try
		{
			final RefList< Link > edgesToInterpolate = RefCollections.createRefList( graph.edges() );
			for ( final Link edge : graph.edges() )
			{
				final Spot from = edge.getSource( vref1 );
				final Spot to = edge.getTarget( vref2 );
				if ( to.getTimepoint() - from.getTimepoint() > 1 )
					edgesToInterpolate.add( edge );
			}

			if ( !edgesToInterpolate.isEmpty() )
			{
				edgesToInterpolate.forEach( this::interpolateEdge );
				model.setUndoPoint();
				graph.notifyGraphChanged();
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	private void interpolateEdge( final Link edge )
	{
		final Spot from = edge.getSource( vref1 );
		final Spot to = edge.getTarget( vref2 );
		graph.remove( edge );

		final int t0 = from.getTimepoint();
		final int t1 = to.getTimepoint();

		final int steps = t1 - t0;
		if( steps >= 2 )
		{
			from.localize( pos0 );
			to.localize( pos1 );
			from.getCovariance( cov0 );
			final double radiusRatio = Math.sqrt( to.getBoundingSphereRadiusSquared() / from.getBoundingSphereRadiusSquared() );

			Spot previous = from;
			for ( int s = 1; s < steps; ++s )
			{
				final double ratio = ( double ) s / steps;

				final int t = t0 + s;
				lerp( pos0, pos1, ratio, pos );
				final double scale = ( 1.0 - ratio ) + ratio * radiusRatio;
				LinAlgHelpers.scale( cov0, scale * scale, cov );

				final Spot current = graph.addVertex( vref3 ).init( t, pos, cov );
				graph.addEdge( previous, current, eref1 ).init();
				previous = vref1.refTo( current );
			}
			graph.addEdge( previous, to, eref1 ).init();
		}
	}


	// TODO: Use imglib2 LinAlgHelpers.lerp() when released
	/**
	 * set c = ( 1 - t ) * a + t * b, where a, b are vectors and t is scalar. Dimensions of a, b, and c
	 * must match. In place interpolation (c==a or c==b) is allowed.
	 */
	private static void lerp( final double[] a, final double[] b, final double t, final double[] c )
	{
		assert rows( a ) == rows( b );
		assert rows( a ) == rows( c );

		final int rows = rows( a );

		for ( int i = 0; i < rows; ++i )
			c[ i ] = ( 1.0 - t ) * a[ i ] + t * b[ i ];
	}
}
