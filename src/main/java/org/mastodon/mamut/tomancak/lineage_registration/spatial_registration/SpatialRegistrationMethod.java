package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

/**
 * An enum that can be used to select between different {@link SpatialRegistration}
 * implementations.
 */
public enum SpatialRegistrationMethod
{
	FIXED_ROOTS( "fixed spatial registration based on root cells" ),
	DYNAMIC_ROOTS( "dynamic spatial registration based on root cells and their descendants" ),
	DYNAMIC_LANDMARKS( "dynamic spatial registration based on \"landmarks\" tag set" );

	private final String toString;

	private SpatialRegistrationMethod( String toString )
	{
		this.toString = toString;
	}

	@Override
	public String toString()
	{
		return toString;
	}

	public static SpatialRegistrationFactory getFactory( SpatialRegistrationMethod method )
	{
		switch ( method )
		{
		case FIXED_ROOTS:
			return FixedSpatialRegistration::forDividingRoots;
		case DYNAMIC_ROOTS:
			return DynamicLandmarkRegistration::forRoots;
		case DYNAMIC_LANDMARKS:
			return ( modelA, modelB, rootsAB ) -> DynamicLandmarkRegistration.forTagSet( modelA, modelB );
		}
		throw new AssertionError();
	}
}
