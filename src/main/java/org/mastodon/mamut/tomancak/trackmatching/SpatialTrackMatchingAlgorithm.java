/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.trackmatching;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.trackmatching.spatial_registration.NotEnoughPairedRootsException;
import org.mastodon.mamut.tomancak.trackmatching.spatial_registration.SpatialRegistration;
import org.mastodon.mamut.tomancak.trackmatching.spatial_registration.SpatialRegistrationFactory;
import org.mastodon.mamut.tomancak.trackmatching.spatial_registration.SpatialRegistrationMethod;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

/**
 * An algorithm that compares the "cell division directions" in two lineages.
 * By doing so it figures out which spots need to be flipped in order
 * to match the TrackSchemes of both lineages.
 *
 * @see SortTreeUtils#directionOfCellDivision(ModelGraph, Spot)
 */
public class SpatialTrackMatchingAlgorithm
{
	private static final int TIME_OFFSET = SortTreeUtils.DIVISION_DIRECTION_TIME_OFFSET;

	private final SpatialRegistration spatialRegistration;

	private final ModelGraph graphA;

	private final ModelGraph graphB;

	/**
	 * Map branch starting spots in graphA to branch starting spots in graphB.
	 */
	private final RefRefMap< Spot, Spot > mapAB;

	/**
	 * A map from branch starting spots in graphA to the angle between the paired cell division directions.
	 */
	private final RefDoubleMap< Spot > angles;

	/**
	 * Runs the spatial track matching for to given graphs. The spots before
	 * the given timepoints are ignored.
	 *
	 * @return a {@link RegisteredGraphs} object that contains the two graphs and the
	 * 	   mapping between the first spots of the branches in the two graphs.
	 */
	public static RegisteredGraphs run(
			Model modelA,
			int firstTimepointA,
			Model modelB,
			int firstTimepointB,
			SpatialRegistrationMethod spatialRegistrationMethod )
	{
		try
		{
			RefRefMap< Spot, Spot > roots =
					RootsPairing.pairDividingRoots( modelA.getGraph(), firstTimepointA, modelB.getGraph(), firstTimepointB );
			SpatialRegistrationFactory algorithm = SpatialRegistrationMethod.getFactory( spatialRegistrationMethod );
			SpatialRegistration spatialRegistration = algorithm.run( modelA, modelB, roots );
			return run( modelA, modelB, roots, spatialRegistration );
		}
		catch ( NotEnoughPairedRootsException e )
		{
			throw newDetailedNotEnoughPairedRootsException( e, modelA, firstTimepointA, modelB, firstTimepointB );
		}
	}

	private static NotEnoughPairedRootsException newDetailedNotEnoughPairedRootsException( NotEnoughPairedRootsException e, Model modelA, int firstTimepointA, Model modelB, int firstTimepointB )
	{
		String message = e.getMessage() + "\n"
				+ "\n"
				+ RootsPairing.report( modelA.getGraph(), firstTimepointA, modelB.getGraph(), firstTimepointB )
				+ "\n"
				+ "Please make sure to:\n"
				+ "  - Select timepoints at which both embryos are at a similar stage\n"
				+ "    and have at least 3 cells that divide.\n"
				+ "  - Name those cells by setting the label of the first spot of the cell.\n"
				+ "    The cell names need to match between the two datasets.\n"
				+ "  - If there are less than three dividing cells, consider using the "
				+ "    dynamic spatial registration that is based on landmarks.\n";
		return new NotEnoughPairedRootsException( message );
	}

	public static RegisteredGraphs run( Model modelA, Model modelB,
			RefRefMap< Spot, Spot > roots, SpatialRegistration spatialRegistration )
	{
		SpatialTrackMatchingAlgorithm algorithm = new SpatialTrackMatchingAlgorithm(
				modelA.getGraph(), modelB.getGraph(),
				roots, spatialRegistration );
		return new RegisteredGraphs( modelA, modelB, spatialRegistration, algorithm.getMapping(), algorithm.getAngles() );
	}

	private RefDoubleMap< Spot > getAngles()
	{
		return angles;
	}

	private SpatialTrackMatchingAlgorithm( ModelGraph graphA, ModelGraph graphB, RefRefMap< Spot, Spot > roots,
			SpatialRegistration spatialRegistration )
	{
		this.spatialRegistration = spatialRegistration;
		this.graphA = graphA;
		this.graphB = graphB;
		this.mapAB = new RefRefHashMap<>( graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
		this.angles = new RefDoubleHashMap<>( graphA.vertices().getRefPool(), Double.NaN );
		Spot refB = graphB.vertexRef();
		try
		{
			for ( Spot rootA : roots.keySet() )
			{
				Spot rootB = roots.get( rootA, refB );
				matchTree( rootA, rootB );
			}
		}
		finally
		{
			graphB.releaseRef( refB );
		}
	}

	private void matchTree( Spot rootA, Spot rootB )
	{
		mapAB.put( rootA, rootB );
		Spot refA = graphA.vertexRef();
		Spot refB = graphB.vertexRef();
		try
		{
			Spot dividingA = BranchGraphUtils.getBranchEnd( rootA, refA );
			Spot dividingB = BranchGraphUtils.getBranchEnd( rootB, refB );
			boolean bothDivide = dividingA.outgoingEdges().size() == 2 &&
					dividingB.outgoingEdges().size() == 2;
			if ( !bothDivide )
				return;
			double[] directionA = SortTreeUtils.directionOfCellDivision( graphA, dividingA );
			double[] directionB = SortTreeUtils.directionOfCellDivision( graphB, dividingB );
			AffineTransform3D transformAB = noOffsetTransform( spatialRegistration.getTransformationAtoB(
					dividingA.getTimepoint() + TIME_OFFSET, dividingB.getTimepoint() + TIME_OFFSET ) );
			transformAB.apply( directionA, directionA );
			double angle = SortTreeUtils.angleInDegree( directionA, directionB );
			angles.put( rootA, angle );
			boolean flip = angle > 90;
			matchChildTree( dividingA, dividingB, 0, flip ? 1 : 0 );
			matchChildTree( dividingA, dividingB, 1, flip ? 0 : 1 );
		}
		finally
		{
			graphA.releaseRef( refA );
			graphB.releaseRef( refB );
		}
	}

	private void matchChildTree( Spot dividingA, Spot dividingB, int indexA, int indexB )
	{
		Spot childA = dividingA.outgoingEdges().get( indexA ).getTarget();
		Spot childB = dividingB.outgoingEdges().get( indexB ).getTarget();
		try
		{
			matchTree( childA, childB );
		}
		finally
		{
			graphA.releaseRef( childA );
			graphB.releaseRef( childB );
		}
	}

	/**
	 * @return a {@link RefRefMap} that maps (branch starting) spots in embryoA
	 * to the matched (branch starting) spots in embryoB.
	 */
	public RefRefMap< Spot, Spot > getMapping()
	{
		return mapAB;
	}

	// -- Helper methods --

	private static AffineTransform3D noOffsetTransform( AffineTransform3D transformAB )
	{
		AffineTransform3D noOffsetTransform = new AffineTransform3D();
		noOffsetTransform.set( transformAB );
		noOffsetTransform.setTranslation( 0, 0, 0 );
		return noOffsetTransform;
	}

}
