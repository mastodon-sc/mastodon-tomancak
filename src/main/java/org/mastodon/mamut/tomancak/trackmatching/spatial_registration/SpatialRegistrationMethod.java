/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.trackmatching.spatial_registration;

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
