package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

/**
 * An enum that can be used to select between different {@link SpacialRegistration}
 * implementations.
 */
public enum SpacialRegistrationMethod
{
	FIXED_ROOTS( "fixed spacial registration based on root cells" ),
	DYNAMIC_ROOTS( "dynamic spacial registration based on root cells and their descendants" ),
	DYNAMIC_LANDMARKS( "dynamic spacial registration based on \"landmarks\" tag set" );

	private final String toString;

	private SpacialRegistrationMethod( String toString )
	{
		this.toString = toString;
	}

	@Override
	public String toString()
	{
		return toString;
	}

	public static SpacialRegistrationFactory getFactory( SpacialRegistrationMethod method )
	{
		switch ( method )
		{
		case FIXED_ROOTS:
			return FixedSpacialRegistration::forDividingRoots;
		case DYNAMIC_ROOTS:
			return DynamicLandmarkRegistration::forRoots;
		case DYNAMIC_LANDMARKS:
			return ( modelA, modelB, rootsAB ) -> DynamicLandmarkRegistration.forTagSet( modelA, modelB );
		}
		throw new AssertionError();
	}
}
