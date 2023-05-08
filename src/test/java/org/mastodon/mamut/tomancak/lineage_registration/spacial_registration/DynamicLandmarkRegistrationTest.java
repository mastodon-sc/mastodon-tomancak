package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.LineageRegistrationAlgorithm;
import org.mastodon.mamut.tomancak.lineage_registration.RegisteredGraphs;
import org.mastodon.mamut.tomancak.lineage_registration.TagSetUtils;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.mastodon.model.tag.TagSetStructure;

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
	public void testForRoots()
	{
		SpacialRegistration spacialRegistration = DynamicLandmarkRegistration.forRoots( embryo1.model, embryo2.model, pairedRoots );
		AffineTransform3D transformation = spacialRegistration.getTransformationAtoB( 2, 2 );
		AffineTransform3D expected = new AffineTransform3D();
		expected.rotate( 2, -2 * rotationPerTimepoint * 2 );
		assertArrayEquals( expected.getRowPackedCopy(), transformation.getRowPackedCopy(), 0.1 );
		// NB: The averaging of the landmark points over time causes the returned
		// transformation not be exactly the rotation matrix.
	}

	@Test
	public void testForTagSet()
	{
		addTags( embryo1 );
		addTags( embryo2 );
		SpacialRegistration spacialRegistration = DynamicLandmarkRegistration.forTagSet( embryo1.model, embryo2.model );
		AffineTransform3D transformation = spacialRegistration.getTransformationAtoB( 2, 2 );
		AffineTransform3D expected = new AffineTransform3D();
		expected.rotate( 2, -2 * rotationPerTimepoint * 2 );
		assertArrayEquals( expected.getRowPackedCopy(), transformation.getRowPackedCopy(), 0.1 );
	}

	private static void addTags( ExampleEmbryo embryo )
	{
		List< Pair< String, Integer > > colors = Arrays.asList(
				Pair.of( "A", Color.red.getRGB() ),
				Pair.of( "B", Color.green.getRGB() ),
				Pair.of( "C", Color.blue.getRGB() ) );
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( embryo.model, "landmarks", colors );
		tagBranches( tagSet, embryo.model, tagSet.getTags().get( 0 ), embryo.a, embryo.a1, embryo.a2 );
		tagBranches( tagSet, embryo.model, tagSet.getTags().get( 1 ), embryo.b, embryo.b1, embryo.b2 );
		tagBranches( tagSet, embryo.model, tagSet.getTags().get( 2 ), embryo.c, embryo.c1, embryo.c2, embryo.c21, embryo.c21 );
	}

	private static void tagBranches( TagSetStructure.TagSet tagSet, Model model, TagSetStructure.Tag tag, Spot... spots )
	{
		for ( Spot spot : spots )
			TagSetUtils.tagBranch( model, tagSet, tag, spot );
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
