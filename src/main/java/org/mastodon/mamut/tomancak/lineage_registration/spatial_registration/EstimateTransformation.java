/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

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

/**
 * This class holds a function for estimating a 3d rigid transformation
 * between two sets of points.
 */
class EstimateTransformation
{

	private EstimateTransformation()
	{
		// prevent instantiation
	}

	/**
	 * <p>
	 * @return a 3d rigid transform (that is composed of scaling, rotation and translation).
	 * The returned transformation is the optimal transformation, that when applied on the "key" spots
	 * of the given "pairs" map minimizes the distance to the "value" spots.
	 * </p>
	 * <p>
	 * See "Closed-form solution of absolute orientation using unit quaternions",
	 * Horn, B. K. P., Journal of the Optical Society of America A, Vol. 4, page 629, April 1987
	 * </p>
	 * @param pairs A map that serves as a list of pairs of spots. Each pair consists of a "key" spot
	 *              and a "value" spot. The algorithm only uses the coordinates of the spots, all other
	 *              properties are ignored.
	 * @see SimilarityModel3D
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
				Spot spotB = pairs.get( spotA, refB );
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

	public static AffineTransform3D fitTransform( List< PointMatch > matches )
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
