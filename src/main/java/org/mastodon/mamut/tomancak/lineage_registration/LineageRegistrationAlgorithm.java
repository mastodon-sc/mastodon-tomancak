package org.mastodon.mamut.tomancak.lineage_registration;

import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

/**
 * An algorithm that by compares the "spindle directions" in two lineages.
 * By doing so it figures out which spots need to be flipped in order
 * to match the TrackSchemes of both lineages.
 */
public class LineageRegistrationAlgorithm
{
	private final AffineTransform3D transformAB;
	
	private final ModelGraph graphA;
	
	private final ModelGraph graphB;
	
	private final RefList< Spot > toBeFlipped;

	public static void run( Model embryoA, Model embryoB )
	{
		ModelGraph graphA = embryoA.getGraph();
		ModelGraph graphB = embryoB.getGraph();
		RefRefMap< Spot, Spot > roots = RootsPairing.pairRoots( graphA, graphB );
		AffineTransform3D transformAB = EstimateTransformation.estimateScaleRotationAndTranslation( roots );
		RefList< Spot > toBeFlipped = new LineageRegistrationAlgorithm(
				graphA, graphB,
				roots, transformAB ).getToBeFlipped();
		FlipDescendants.flipDescendants( embryoB, toBeFlipped );
	}

	public LineageRegistrationAlgorithm( ModelGraph graphA, ModelGraph graphB, RefRefMap< Spot, Spot > roots,
			AffineTransform3D transformAB ) {
		this.transformAB = noOffsetTransform( transformAB );
		this.graphA = graphA;
		this.graphB = graphB;
		this.toBeFlipped = new RefArrayList<>( graphB.vertices().getRefPool() );
		for( Spot rootA : roots.keySet() ) {
			Spot rootB = roots.get( rootA );
			matchTree( rootA, rootB );
		}
	}

	private void matchTree(Spot rootA, Spot rootB)
	{
		Spot dividingA = LineageTreeUtils.getBranchEnd( graphA, rootA );
		Spot dividingB = LineageTreeUtils.getBranchEnd( graphB, rootB );
		try
		{
			if(dividingA.outgoingEdges().size() != 2 ||
			  dividingB.outgoingEdges().size() != 2)
				return;
			double[] directionA = SortTreeUtils.directionOfCellDevision( graphA, dividingA );
			double[] directionB = SortTreeUtils.directionOfCellDevision( graphB, dividingB );
			transformAB.apply( directionA, directionA );
			boolean flip = SortTreeUtils.scalarProduct( directionA, directionB ) < 0;
			if(flip)
				toBeFlipped.add( dividingB );
			matchChildTree( dividingA, dividingB, 0, flip ? 1 : 0 );
			matchChildTree( dividingA, dividingB, 1, flip ? 0 : 1 );
		} finally
		{
			graphA.releaseRef( dividingA );
			graphB.releaseRef( dividingB );
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

	public RefList< Spot > getToBeFlipped()
	{
		return toBeFlipped;
	}
	
	private static AffineTransform3D noOffsetTransform( AffineTransform3D transformAB )
	{
		AffineTransform3D noOffsetTransform = new AffineTransform3D();
		noOffsetTransform.set( transformAB );
		noOffsetTransform.setTranslation( 0, 0, 0 );
		return noOffsetTransform;
	}
}
