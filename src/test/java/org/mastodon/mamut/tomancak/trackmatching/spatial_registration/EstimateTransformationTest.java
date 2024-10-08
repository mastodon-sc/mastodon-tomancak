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
package org.mastodon.mamut.tomancak.trackmatching.spatial_registration;

import static org.junit.Assert.assertArrayEquals;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class EstimateTransformationTest
{
	@Test
	public void testEstimateScaleRotationTransform()
	{
		AffineTransform3D expected = exampleTransformation();
		ModelGraph graphA = simpleGraph();
		// initialize a transformed graphB and a map of spot pairs
		ModelGraph graphB = new ModelGraph();
		RefRefMap< Spot, Spot > pairs = new RefRefHashMap<>( graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
		for ( Spot spotA : graphA.vertices() )
		{
			Spot spotB = graphB.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
			expected.apply( spotA, spotB );
			pairs.put( spotA, spotB );
		}
		// estimate transform
		AffineTransform3D result = EstimateTransformation.estimateScaleRotationAndTranslation( pairs );
		// test
		assertArrayEquals( asArray( expected ), asArray( result ), 0.001 );
	}

	private ModelGraph simpleGraph()
	{
		ModelGraph graphA = new ModelGraph();
		graphA.addVertex().init( 0, new double[] { 1, 0, 0 }, 1 );
		graphA.addVertex().init( 0, new double[] { 0, 1, 0 }, 1 );
		graphA.addVertex().init( 0, new double[] { 0, 0, 1 }, 1 );
		return graphA;
	}

	private AffineTransform3D exampleTransformation()
	{
		AffineTransform3D transform = new AffineTransform3D();
		transform.rotate( 0, Math.PI / 7 );
		transform.rotate( 1, Math.PI / 7 );
		transform.rotate( 2, Math.PI / 7 );
		transform.scale( 2 );
		transform.translate( 7, 6, 8 );
		return transform;
	}

	private double[] asArray( AffineTransform3D transform )
	{
		double[] data = new double[ 12 ];
		transform.toArray( data );
		return data;
	}
}
