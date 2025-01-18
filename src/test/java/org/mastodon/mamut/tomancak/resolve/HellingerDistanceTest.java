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
package org.mastodon.mamut.tomancak.resolve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HellingerDistanceTest
{
	@Test
	public void testHellingerDistance()
	{
		final double[] mean = new double[] { 1, 2, 3 };
		final double[][] cov = diagonal( 4.0, 5.0, 6.0 );
		assertEquals( 0, HellingerDistance.hellingerDistance( mean, cov, mean, cov ), 1e-10 );
		assertEquals( 1, HellingerDistance.hellingerDistance( mean, cov, new double[] { 100, 100, 100 }, cov ), 1e-10 );
		assertEquals( 0.50, HellingerDistance.hellingerDistance( mean, cov, new double[] { 4, 2, 3 }, cov ), 0.01 );
		assertEquals( 0.32, HellingerDistance.hellingerDistance( mean, cov, mean, diagonal( 1, 5, 6 ) ), 0.01 );
	}

	static double[][] diagonal( final double a, final double b, final double c )
	{
		return new double[][] { { a, 0, 0 }, { 0, b, 0 }, { 0, 0, c } };
	}
}
