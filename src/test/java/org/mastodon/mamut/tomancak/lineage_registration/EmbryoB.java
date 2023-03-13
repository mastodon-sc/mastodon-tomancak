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
	EmbryoB()
	{
		transformSpotPositions();
		flipB1andB2Positions();
	}

	private void transformSpotPositions()
	{
		// transform spot positions: rotate 90 degrees around x-axis
		AffineTransform3D transform = new AffineTransform3D();
		transform.rotate( 0, Math.PI / 2 );
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
