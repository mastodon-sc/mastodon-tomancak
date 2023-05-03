package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import net.imglib2.realtransform.AffineTransform3D;

public interface SpacialRegistration
{

	AffineTransform3D getTransformationAtoB( int timepointA, int timepointB );
}
