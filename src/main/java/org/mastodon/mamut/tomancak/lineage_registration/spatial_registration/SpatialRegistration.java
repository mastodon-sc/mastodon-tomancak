package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

import net.imglib2.realtransform.AffineTransform3D;

public interface SpatialRegistration
{

	AffineTransform3D getTransformationAtoB( int timepointA, int timepointB );
}
