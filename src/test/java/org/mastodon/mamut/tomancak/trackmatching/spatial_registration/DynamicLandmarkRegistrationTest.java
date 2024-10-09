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
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.trackmatching.SpatialTrackMatchingAlgorithm;
import org.mastodon.mamut.tomancak.trackmatching.RegisteredGraphs;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

/**
 * Tests {@link DynamicLandmarkRegistration} and it's integration in the
 * {@link SpatialTrackMatchingAlgorithm}.
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
		SpatialRegistration spatialRegistration = DynamicLandmarkRegistration.forRoots( embryo1.model, embryo2.model, pairedRoots );
		AffineTransform3D transformation = spatialRegistration.getTransformationAtoB( 2, 2 );
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
		SpatialRegistration spatialRegistration = DynamicLandmarkRegistration.forTagSet( embryo1.model, embryo2.model );
		AffineTransform3D transformation = spatialRegistration.getTransformationAtoB( 2, 2 );
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
		TagHelper tagA = new TagHelper( embryo.model, tagSet, "A" );
		tagA.tagBranch( embryo.a );
		tagA.tagBranch( embryo.a1 );
		tagA.tagBranch( embryo.a2 );
		TagHelper tagB = new TagHelper( embryo.model, tagSet, "B" );
		tagB.tagBranch( embryo.b );
		tagB.tagBranch( embryo.b1 );
		tagB.tagBranch( embryo.b2 );
		TagHelper tagC = new TagHelper( embryo.model, tagSet, "C" );
		tagC.tagBranch( embryo.c );
		tagC.tagBranch( embryo.c1 );
		tagC.tagBranch( embryo.c2 );
		tagC.tagBranch( embryo.c21 );
		tagC.tagBranch( embryo.c22 );
	}

	@Test
	public void testLineageRegistrationAlgorithm()
	{
		RegisteredGraphs result = SpatialTrackMatchingAlgorithm.run( embryo1.model, 0, embryo2.model, 0,
				SpatialRegistrationMethod.DYNAMIC_ROOTS );
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
