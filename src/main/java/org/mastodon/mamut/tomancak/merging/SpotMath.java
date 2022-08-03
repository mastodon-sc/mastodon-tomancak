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
package org.mastodon.mamut.tomancak.merging;

import static net.imglib2.util.LinAlgHelpers.cols;
import static net.imglib2.util.LinAlgHelpers.rows;

import org.mastodon.mamut.model.Spot;

import net.imglib2.util.LinAlgHelpers;

/**
 * Adapted from EllipsoidInsideTest, which was in turn adapted from ScreenVertexMath.
 * TODO: is there potential for unification?
 */
public class SpotMath
{
	private final double[] pos1 = new double[ 3 ];
	private final double[] diff = new double[ 3 ];
	private final double[][] cov = new double[ 3 ][ 3 ];
	private final double[][] P = new double[ 3 ][ 3 ];

	/**
	 * Returns {@code true} if the center of {@code s2} is contained in
	 * {@code s1} ellipsoid.
	 * 
	 * @param s1
	 *            the first spot.
	 * @param s2
	 *            the second spot.
	 * @return <code>true</code> if <code>s2</code> center is contained in
	 *         <code>s1</code> ellipsoid.
	 */
	public boolean containsCenter( final Spot s1, final Spot s2 )
	{
		return mahalanobisDistSqu( s1, s2 ) < 1.0;
	}

	/**
	 * Returns the squared Mahalanobis distance of the center of {@code s2} to
	 * {@code s1} ellipsoid.
	 * 
	 * @param s1
	 *            the first spot.
	 * @param s2
	 *            the second spot.
	 * @return the squared Mahalanobis distance.
	 */
	public double mahalanobisDistSqu( final Spot s1, final Spot s2 )
	{
		s1.localize( pos1 );
		s1.getCovariance( cov );
		LinAlgHelpers.invertSymmetric3x3( cov, P );

		s2.localize( diff );
		LinAlgHelpers.subtract( diff, pos1, diff );
		return multSymmetric3x3bAb( P, diff );
	}

	static double multSymmetric3x3bAb( final double[][] A, final double[] b )
	{
		assert cols( A ) == 3;
		assert rows( A ) == 3;
		assert rows( b ) == 3;

		final double x = b[ 0 ];
		final double y = b[ 1 ];
		final double z = b[ 2 ];
		return A[ 0 ][ 0 ] * x * x + A[ 1 ][ 1 ] * y * y + A[ 2 ][ 2 ] * z * z + 2 * ( A[ 0 ][ 1 ] * x * y + A[ 0 ][ 2 ] * x * z + A[ 1 ][ 2 ] * y * z );
	}
}
