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
		assertEquals( 0.25, HellingerDistance.hellingerDistance( mean, cov, new double[] { 4, 2, 3 }, cov ), 0.01 );
		assertEquals( 0.10, HellingerDistance.hellingerDistance( mean, cov, mean, diagonal( 1, 5, 6 ) ), 0.01 );
	}

	private static double[][] diagonal( final double a, final double b, final double c )
	{
		return new double[][] { { a, 0, 0 }, { 0, b, 0 }, { 0, 0, c } };
	}
}
