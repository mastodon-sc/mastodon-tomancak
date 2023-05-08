package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.LineageRegistrationAlgorithm;
import org.mastodon.mamut.tomancak.lineage_registration.RegisteredGraphs;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

/**
 * Tests {@link DynamicLandmarkRegistration} and it's integration in the
 * {@link LineageRegistrationAlgorithm}.
 */
public class DynamicLandmarkRegistrationTest
{

	private ExampleEmbryo embryo1;

	private ExampleEmbryo embryo2;

	private double rotationPerTimepoint;

	private RefRefMap< Spot, Spot > pairedRoots;

	@Before
	public void before()
	{
		// init embryo 1
		embryo1 = new ExampleEmbryo();
		rotationPerTimepoint = 0.5 * Math.PI / ( SortTreeUtils.getNumberOfTimePoints( embryo1.graph ) - 1 );
		rotateGraphPerTimepoint( embryo1.graph, +rotationPerTimepoint );

		// init embryo 2
		embryo2 = new ExampleEmbryo();
		rotateGraphPerTimepoint( embryo2.graph, -rotationPerTimepoint );

		// init paired roots
		pairedRoots = new RefRefHashMap<>( embryo1.graph.vertices().getRefPool(), embryo2.graph.vertices().getRefPool() );
		pairedRoots.put( embryo1.a, embryo2.a );
		pairedRoots.put( embryo1.b, embryo2.b );
		pairedRoots.put( embryo1.c, embryo2.c );
	}

	@Test
	public void testGetTransformationAtoB()
	{
		SpacialRegistration spacialRegistration = new DynamicLandmarkRegistration( embryo1.model, embryo2.model, pairedRoots );
		AffineTransform3D transformation = spacialRegistration.getTransformationAtoB( 2, 2 );
		AffineTransform3D expected = new AffineTransform3D();
		expected.rotate( 2, -2 * rotationPerTimepoint * 2 );
		assertArrayEquals( expected.getRowPackedCopy(), transformation.getRowPackedCopy(), 0.1 );
		// NB: The averaging of the landmark points over time causes the returned
		// transformation not be exactly the rotation matrix.
	}

	@Test
	public void testLineageRegistrationAlgorithm()
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( embryo1.model, 0, embryo2.model, 0,
				SpacialRegistrationMethod.DYNAMIC_ROOTS );
		assertEquals( embryo2.c21, result.mapAB.get( embryo1.c21 ) );
		assertEquals( embryo2.c22, result.mapAB.get( embryo1.c22 ) );
	}

	static void rotateGraphPerTimepoint( ModelGraph graph, double rotationPerTimepoint )
	{
		int numberOfTimepoints = SortTreeUtils.getNumberOfTimePoints( graph );

		List< AffineTransform3D > rotations = new ArrayList<>();
		for ( int t = 0; t < numberOfTimepoints; t++ )
		{
			AffineTransform3D transform = new AffineTransform3D();
			transform.rotate( 2, rotationPerTimepoint * t );
			rotations.add( transform );
		}

		for ( Spot spot : graph.vertices() )
			rotations.get( spot.getTimepoint() ).apply( spot, spot );
	}
}
