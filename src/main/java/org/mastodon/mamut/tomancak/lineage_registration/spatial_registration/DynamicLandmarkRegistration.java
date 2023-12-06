/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.RefMapUtils;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;

import mpicbg.models.Point;
import mpicbg.models.PointMatch;

public class DynamicLandmarkRegistration implements SpatialRegistration
{

	/**
	 * The algorithm uses a rolling average to stabilize the landmarks
	 * position over time. The actual window size used is
	 * {@code 2 * HALF_WINDOW_SIZE + 1}.
	 * <p>
	 * (This averaging method was found to work best for the lineage
	 * registration on some test datasets of macrostomum lignano embryos.)
	 */
	private static final int HALF_WINDOW_SIZE = 2;

	/**
	 * Landmark positions that are used to compute the registration.
	 * <p>
	 * A list of type {@code List< double[] >} is thereby used to
	 * hold a landmarks position as it changes over time. The i-th
	 * element of the list is the position of the landmark at timepoint i.
	 * <p>
	 * The "left" value in each pair belongs to landmark in {@code modelA}.
	 * The "right" value belongs to the respective landmark in {@code modelB}.
	 */
	private final List< Pair< List< double[] >, List< double[] > > > landmarks;

	/** The number of timepoints in {@code modelA}. */
	private final int numTimepointsA;

	/** The number of timepoints in {@code modelB}. */
	private final int numTimepointsB;

	/**
	 * Initializes a {@link DynamicLandmarkRegistration}. The given pairs of roots
	 * and all their descendants positions are used to compute a registration
	 * between the two given {@link Model models}.
	 */
	public static DynamicLandmarkRegistration forRoots( Model modelA, Model modelB, RefRefMap< Spot, Spot > rootsAB )
	{
		if ( rootsAB.size() < 3 )
			throw new NotEnoughPairedRootsException();

		DynamicLandmarkRegistration dynamicLandmarkRegistration = new DynamicLandmarkRegistration( modelA.getGraph(), modelB.getGraph() );
		RefMapUtils.forEach( rootsAB, ( rootA, rootB ) -> {
			Collection< Spot > descendantsA = getDescendants( modelA.getGraph(), rootA );
			Collection< Spot > descendantsB = getDescendants( modelB.getGraph(), rootB );
			dynamicLandmarkRegistration.addLandmark( descendantsA, descendantsB );
		} );
		return dynamicLandmarkRegistration;
	}

	/**
	 * Initializes a {@link DynamicLandmarkRegistration}. Each model must have a
	 * tag set named "landmarks" that contains the tags to be used for registration.
	 * Both tag sets must contain the same tags.
	 */
	public static DynamicLandmarkRegistration forTagSet( Model modelA, Model modelB )
	{
		Map< String, TagSetStructure.Tag > tagSetA =
				tagSetAsMap( TagSetUtils.findTagSet( modelA, "landmarks" ) );

		Map< String, TagSetStructure.Tag > tagSetB =
				tagSetAsMap( TagSetUtils.findTagSet( modelB, "landmarks" ) );

		DynamicLandmarkRegistration dynamicLandmarkRegistration = new DynamicLandmarkRegistration( modelA.getGraph(), modelB.getGraph() );
		for ( String tagLabel : tagSetA.keySet() )
			if ( tagSetB.containsKey( tagLabel ) )
			{
				Collection< Spot > landmarkA = modelA.getTagSetModel().getVertexTags().getTaggedWith( tagSetA.get( tagLabel ) );
				Collection< Spot > landmarkB = modelB.getTagSetModel().getVertexTags().getTaggedWith( tagSetB.get( tagLabel ) );
				boolean valid = !landmarkA.isEmpty() && !landmarkB.isEmpty();
				if ( valid )
					dynamicLandmarkRegistration.addLandmark( landmarkA, landmarkB );
			}

		if ( dynamicLandmarkRegistration.landmarks.size() < 3 )
			throwNotEnoughLandmarksException( tagSetA, tagSetB );

		return dynamicLandmarkRegistration;
	}

	private static void throwNotEnoughLandmarksException( Map< String, TagSetStructure.Tag> tagSetA, Map< String, TagSetStructure.Tag> tagSetB )
	{
		Set< String > intersection = new HashSet<>( tagSetA.keySet() );
		intersection.retainAll( tagSetB.keySet() );
		String message = "Not enough landmarks to compute a coordinate transform.\n"
				+ "The algorithm requires at least 3 landmarks, that are present in both datasets.\n"
				+ "\n"
				+ "Please add more landmarks, and tag them."
				+ "Make sure that tag labels are the same in both datasets.\n"
				+ "\n"
				+ "Tags of the \"landmarks\" tag set in the first dataset:\n"
				+ "    " + tagSetA.keySet() + "\n"
				+ "Tags of the \"landmarks\" tag set in the second dataset:\n"
				+ "    " + tagSetB.keySet() + "\n"
				+ "Only " + intersection.size() + " tag sets could be paired based on their name, but 3 are required:\n"
				+ "    " + intersection + "\n";
		throw new NotEnoughPairedTagsException( message );
	}

	public DynamicLandmarkRegistration( ModelGraph graphA, ModelGraph graphB )
	{
		numTimepointsA = SortTreeUtils.getNumberOfTimePoints( graphA );
		numTimepointsB = SortTreeUtils.getNumberOfTimePoints( graphB );
		landmarks = new ArrayList<>();
	}

	private void addLandmark( Collection< Spot > descendantsA, Collection< Spot > descendantsB )
	{
		List< double[] > landmarkA = SortTreeUtils.calculateAndInterpolateAveragePosition( numTimepointsA, descendantsA );
		List< double[] > landmarkB = SortTreeUtils.calculateAndInterpolateAveragePosition( numTimepointsB, descendantsB );
		List< double[] > rollingAverageA = rollingAverage( landmarkA );
		List< double[] > rollingAverageB = rollingAverage( landmarkB );
		landmarks.add( Pair.of( rollingAverageA, rollingAverageB ) );
	}

	private static Collection< Spot > getDescendants( ModelGraph graph, Spot spot )
	{
		RefSet< Spot > descendants = new RefSetImp<>( graph.vertices().getRefPool() );
		Iterator< Spot > iterator = new DepthFirstIterator<>( spot, graph );
		while ( iterator.hasNext() )
			descendants.add( iterator.next() );
		return descendants;
	}

	@Override
	public AffineTransform3D getTransformationAtoB( int timepointA, int timepointB )
	{
		List< PointMatch > matches = new ArrayList<>();
		landmarks.forEach( pair -> {
			List< double[] > landmarkA = pair.getLeft();
			List< double[] > landmarkB = pair.getRight();
			Point pointA = new Point( get( landmarkA, timepointA ) );
			Point pointB = new Point( get( landmarkB, timepointB ) );
			matches.add( new PointMatch( pointA, pointB ) );
		} );
		return EstimateTransformation.fitTransform( matches );
	}

	// -- Helper methods --

	private static List< double[] > rollingAverage( List< double[] > list )
	{
		List< double[] > output = new ArrayList<>( list.size() );
		for ( int i = 0; i < list.size(); i++ )
		{
			double[] array = new double[ 3 ];
			for ( int j = i - HALF_WINDOW_SIZE; j <= i + HALF_WINDOW_SIZE; j++ )
				LinAlgHelpers.add( array, get( list, j ), array );
			SortTreeUtils.divide( array, 2 * HALF_WINDOW_SIZE + 1 );
			output.add( array );
		}
		return output;
	}

	/**
	 * Similar to {@code list.get( i )} but returns the first or last element
	 * instead of throwing an {@link IndexOutOfBoundsException}.
	 */
	private static double[] get( List< double[] > list, int j )
	{
		if ( j < 0 )
			return list.get( 0 );
		if ( j >= list.size() )
			return list.get( list.size() - 1 );
		return list.get( j );
	}

	/**
	 * Returns a map of all tags in the given tag set, indexed by their label.
	 */
	public static Map< String, TagSetStructure.Tag > tagSetAsMap( TagSetStructure.TagSet tagSet )
	{
		return tagSet.getTags().stream()
				.collect( Collectors.toMap( TagSetStructure.Tag::label, tag -> tag ) );
	}
}
