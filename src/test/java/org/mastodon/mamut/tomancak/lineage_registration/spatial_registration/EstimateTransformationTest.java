package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

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
