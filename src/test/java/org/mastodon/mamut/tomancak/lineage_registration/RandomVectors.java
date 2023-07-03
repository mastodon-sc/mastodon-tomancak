package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.Random;

import net.imglib2.util.LinAlgHelpers;

import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

public class RandomVectors
{
	public static void main( String... args )
	{
		Random random = new Random( 0 );
		double sum = 0;
		int count = 0;
		for ( int i = 0; i < 1000; i++ )
		{
			double[] v = randomDirection( random );
			double[] w = randomDirection( random );
			double angle = SortTreeUtils.angleInDegree( v, w );
			sum += angle > 90 ? 180 - angle : angle;
			count++;
		}
		System.out.println( sum / count );
	}

	private static double[] randomDirection( Random random )
	{
		for ( ; ; )
		{
			double[] vector = new double[] { random.nextGaussian(), random.nextGaussian(), random.nextGaussian() };
			if ( LinAlgHelpers.length( vector ) < 0.2 )
				continue;
			LinAlgHelpers.normalize( vector );
			return vector;
		}
	}
}
