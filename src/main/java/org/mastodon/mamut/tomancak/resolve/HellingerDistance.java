package org.mastodon.mamut.tomancak.resolve;

import net.imglib2.util.LinAlgHelpers;

import org.mastodon.mamut.model.Spot;

/**
 * Compute the Hellinger distance.
 */
public class HellingerDistance
{
	private HellingerDistance()
	{
		// prevent instantiation
	}

	/**
	 * @return The Hellinger distance of the two gaussian distributions associated
	 * with the two specified spots. The distance is in [0,1], where 0 means the
	 * two spots are identical, and 1 means they are completely different.
	 * <br>
	 * The Hellinger distance can be used as an indicator of roughly how much two
	 * spots overlap.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Hellinger_distance">Hellinger distance - Wikipedia</a>
	 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3582582/figure/F11/">Hellinger distance illustration</a>
	 */
	public static double hellingerDistance( final Spot a, final Spot b )
	{
		return hellingerDistance( a.positionAsDoubleArray(), covariance( a ), b.positionAsDoubleArray(), covariance( b ) );
	}

	public static double hellingerDistance( final double[] mean1, final double[][] cov1, final double[] mean2, final double[][] cov2 )
	{
		return Math.sqrt( 1 - bhattacharyyaCoefficient( mean1, cov1, mean2, cov2 ) );
	}

	private static double bhattacharyyaCoefficient( final double[] mean1, final double[][] cov1, final double[] mean2, final double[][] cov2 )
	{
		check3( mean1 );
		check3( mean2 );
		check3x3( cov1 );
		check3x3( cov2 );
		final double[][] averageCov = average( cov1, cov2 );
		final double[] diff = subtract( mean1, mean2 );
		return Math.sqrt( Math.sqrt( det( cov1 ) * det( cov2 ) ) / det( averageCov ) )
				* Math.exp( -0.125 * multiply( diff, invert( averageCov ), diff ) );
	}

	private static void check3( final double[] mean ) {
		if ( mean.length != 3 )
			throw new IllegalArgumentException( "Mean must be 3D." );
	}

	private static void check3x3( final double[][] matrix )
	{
		if ( matrix.length != 3 || matrix[ 0 ].length != 3 )
			throw new IllegalArgumentException( "Covariance matrix must be 3x3." );
	}

	private static double[][] average( final double[][] cov1, final double[][] cov2 )
	{
		final double[][] averageCov = new double[ cov1.length ][ cov1[ 0 ].length ];
		LinAlgHelpers.add( cov1, cov2, averageCov );
		LinAlgHelpers.scale( averageCov, 0.5, averageCov );
		return averageCov;
	}

	private static double[][] invert( final double[][] averageCov )
	{
		final double[][] inverse = new double[ averageCov.length ][ averageCov[ 0 ].length ];
		LinAlgHelpers.invertSymmetric3x3( averageCov, inverse );
		return inverse;
	}

	private static double det( final double[][] averageCov )
	{
		return LinAlgHelpers.det3x3(
				averageCov[ 0 ][ 0 ], averageCov[ 0 ][ 1 ], averageCov[ 0 ][ 2 ],
				averageCov[ 1 ][ 0 ], averageCov[ 1 ][ 1 ], averageCov[ 1 ][ 2 ],
				averageCov[ 2 ][ 0 ], averageCov[ 2 ][ 1 ], averageCov[ 2 ][ 2 ] );
	}

	private static double multiply( final double[] a, final double[][] B, final double[] b )
	{
		final double[] result = new double[ a.length ];
		LinAlgHelpers.mult( B, a, result );
		return LinAlgHelpers.dot( result, b );
	}

	private static double[] subtract( final double[] mean1, final double[] mean2 )
	{
		final double[] diff = new double[ mean1.length ];
		LinAlgHelpers.subtract( mean1, mean2, diff );
		return diff;
	}

	private static double[][] covariance( final Spot spot )
	{
		final double[][] cov = new double[ 3 ][ 3 ];
		spot.getCovariance( cov );
		return cov;
	}
}
