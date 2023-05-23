package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

import net.imglib2.realtransform.AffineTransform3D;

public class InverseSpatialRegistration implements SpatialRegistration
{
	private final SpatialRegistration forward;

	public InverseSpatialRegistration( SpatialRegistration forward )
	{
		this.forward = forward;
	}

	@Override
	public AffineTransform3D getTransformationAtoB( int timepointA, int timepointB )
	{
		return forward.getTransformationAtoB( timepointB, timepointA ).inverse();
	}
}
