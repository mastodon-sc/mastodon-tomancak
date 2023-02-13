package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;

import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.models.SimilarityModel3D;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Spot;

public class EstimateTransformation
{

	/**
	 * Return a affine transform, that is composed of scaling, rotation and translation operation.
	 * The transformation is optimized to minimize the distances of the transformed "key" spots
	 * to the "value" spots.
	 */
	public static AffineTransform3D estimateScaleRotationAndTranslation( RefRefMap< Spot, Spot > pairs )
	{
		// NB: This method is not as memory efficient as it could be.
		// It creates multiple objects (Point, PointMatch, arrays) per item in "pairs".
		// Memory efficiency should be improved if performance problems arise.
		Spot refB = pairs.createValueRef();
		try
		{
			List< PointMatch > matches = new ArrayList<>( pairs.size() );
			for ( Spot spotA : pairs.keySet() )
			{
				Spot spotB = pairs.get( spotA );
				Point pointA = new Point( spotA.positionAsDoubleArray() );
				Point pointB = new Point( spotB.positionAsDoubleArray() );
				matches.add( new PointMatch( pointA, pointB, 1 ) );
			}
			return fitTransform( matches );
		}
		finally
		{
			pairs.releaseValueRef( refB );
		}
	}

	private static AffineTransform3D fitTransform( List< PointMatch > matches )
	{
		try
		{
			SimilarityModel3D model = new SimilarityModel3D();
			model.fit( matches );
			AffineTransform3D transform = new AffineTransform3D();
			transform.set( model.getMatrix( null ) );
			return transform;
		}
		catch ( NotEnoughDataPointsException | IllDefinedDataPointsException e )
		{
			throw new RuntimeException( e );
		}
	}
}
