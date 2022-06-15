package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.junit.Test;

public class EstimateTransformationTest
{
	@Test
	public void testEstimateScaleRotationTransform() {
		AffineTransform3D expected = new AffineTransform3D();
		expected.rotate( 0, Math.PI / 7 );
		expected.rotate( 1, Math.PI / 7 );
		expected.rotate( 2, Math.PI / 7 );
		expected.scale( 2 );
		expected.translate( 7,6,8 );
		List<RealPoint> a = Arrays.asList( point(1, 0, 0), point( 0, 1, 0 ), point( 0, 0, 1), point(0, 0,0) );
		List<RealPoint> b = transformPoints( expected, a );
		AffineTransform3D m = EstimateTransformation.estimateScaleRotationAndTranslation(a, b);
		EstimateTransformationTest.assertTransformEquals( expected, m, 0.001 );
	}

	private static RealPoint point( double... values )
	{
		return new RealPoint(values);
	}

	private static void assertTransformEquals( AffineTransform3D expected, AffineTransform3D actual, double tolerance )
	{
		for ( int row = 0; row < 3; row++ )
			for ( int col = 0; col < 4; col++ )
				assertEquals( expected.get( row, col ), actual.get( row, col ), tolerance );
	}
	
	private List<RealPoint> transformPoints( AffineTransform3D transform, List<RealPoint> points )
	{
		List<RealPoint> b = new ArrayList<>();
		for(RealPoint p : points )
		{
			RealPoint r = new RealPoint( 3 );
			transform.apply( p, r );
			b.add( r );
		}
		return b;
	}
}
