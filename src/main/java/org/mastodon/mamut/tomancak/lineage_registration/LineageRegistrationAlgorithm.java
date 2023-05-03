package org.mastodon.mamut.tomancak.lineage_registration;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spacial_registration.FixedSpacialRegistration;
import org.mastodon.mamut.tomancak.lineage_registration.spacial_registration.SpacialRegistration;
import org.mastodon.mamut.tomancak.lineage_registration.spacial_registration.SpacialRegistrationFactory;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

/**
 * An algorithm that compares the "cell division directions" in two lineages.
 * By doing so it figures out which spots need to be flipped in order
 * to match the TrackSchemes of both lineages.
 *
 * @see SortTreeUtils#directionOfCellDevision(ModelGraph, Spot)
 */
public class LineageRegistrationAlgorithm
{
	private final SpacialRegistration spacialRegistration;

	private final ModelGraph graphA;

	private final ModelGraph graphB;

	/**
	 * Map branch starting spots in graphA to branch starting spots in graphB.
	 */
	private final RefRefMap< Spot, Spot > mapAB;

	/**
	 * Runs the lineage registration algorithm for to given graphs. The spots before
	 * the given timepoints are ignored.
	 *
	 * @return a {@link RegisteredGraphs} object that contains the two graphs and the
	 * 	   mapping between the first spots of the branches in the two graphs.
	 */
	public static RegisteredGraphs run( Model modelA, int firstTimepointA, Model modelB, int firstTimepointB )
	{
		RefRefMap< Spot, Spot > roots =
				RootsPairing.pairDividingRoots( modelA.getGraph(), firstTimepointA, modelB.getGraph(), firstTimepointB );
		SpacialRegistrationFactory algorithm = FixedSpacialRegistration::forDividingRoots;
		SpacialRegistration spacialRegistration = algorithm.run( modelA, modelB, roots );
		return run( modelA.getGraph(), modelB.getGraph(), roots, spacialRegistration );
	}

	public static RegisteredGraphs run( ModelGraph graphA, ModelGraph graphB,
			RefRefMap< Spot, Spot > roots, SpacialRegistration transformAB )
	{
		RefRefMap< Spot, Spot > mapping = new LineageRegistrationAlgorithm(
				graphA, graphB,
				roots, transformAB ).getMapping();
		return new RegisteredGraphs( graphA, graphB, transformAB, mapping );
	}

	private LineageRegistrationAlgorithm( ModelGraph graphA, ModelGraph graphB, RefRefMap< Spot, Spot > roots,
			SpacialRegistration spacialRegistration )
	{
		this.spacialRegistration = spacialRegistration;
		this.graphA = graphA;
		this.graphB = graphB;
		this.mapAB = new RefRefHashMap<>( graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
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
			double[] directionA = SortTreeUtils.directionOfCellDevision( graphA, dividingA );
			double[] directionB = SortTreeUtils.directionOfCellDevision( graphB, dividingB );
			AffineTransform3D transformAB = noOffsetTransform( spacialRegistration.getTransformationAtoB(
					dividingA.getTimepoint(), dividingB.getTimepoint() ) );
			transformAB.apply( directionA, directionA );
			boolean flip = SortTreeUtils.scalarProduct( directionA, directionB ) < 0;
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
