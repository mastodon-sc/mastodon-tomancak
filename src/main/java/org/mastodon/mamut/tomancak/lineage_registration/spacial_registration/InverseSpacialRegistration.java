package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import net.imglib2.realtransform.AffineTransform3D;

public class InverseSpacialRegistration implements SpacialRegistration
{
	private final SpacialRegistration forward;

	public InverseSpacialRegistration( SpacialRegistration forward )
	{
		this.forward = forward;
	}

	@Override
	public AffineTransform3D getTransformationAtoB( int timepointA, int timepointB )
	{
		return forward.getTransformationAtoB( timepointB, timepointA ).inverse();
	}
}
