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

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.mamut.model.Spot;

/**
 * Example data for testing {@link LineageRegistrationAlgorithm} and {@link LineageRegistrationUtils}.
 * <p>
 * Very similar to {@link EmbryoA}, but rotated 90 degrees around x-axis and
 * the positions of the spots B1 and B2 are flipped.
 */
class EmbryoB extends EmbryoA
{

	public final AffineTransform3D transform;

	EmbryoB()
	{
		this( 0 );
	}

	EmbryoB( int startTime )
	{
		super( startTime );
		transform = new AffineTransform3D();
		transform.rotate( 0, Math.PI / 2 );
		transformSpotPositions();
		flipB1andB2Positions();
	}

	private void transformSpotPositions()
	{
		// transform spot positions: rotate 90 degrees around x-axis
		for ( Spot spot : graph.vertices() )
			transform.apply( spot, spot );
	}

	private void flipB1andB2Positions()
	{
		double[] v1 = new double[ 3 ];
		double[] v2 = new double[ 3 ];
		b1.localize( v1 );
		b2.localize( v2 );
		b1.setPosition( v2 );
		b2.setPosition( v1 );
	}
}
