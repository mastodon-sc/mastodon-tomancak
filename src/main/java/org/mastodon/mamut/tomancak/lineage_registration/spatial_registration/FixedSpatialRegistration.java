package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.BranchGraphUtils;
import org.mastodon.mamut.tomancak.lineage_registration.RefMapUtils;

public class FixedSpatialRegistration implements SpatialRegistration
{
	private final AffineTransform3D transformAB;

	public static SpatialRegistration forDividingRoots( Model modelA, Model modelB, RefRefMap< Spot, Spot > rootsAB )
	{
		if ( rootsAB.size() < 3 )
			throw new NotEnoughPairedRootsException();
		RefRefMap< Spot, Spot > rootsDividingSpots = getBranchEnds( rootsAB, modelA.getGraph(), modelB.getGraph() );
		AffineTransform3D transformAB = EstimateTransformation.estimateScaleRotationAndTranslation( rootsDividingSpots );
		return new FixedSpatialRegistration( transformAB );
	}

	public FixedSpatialRegistration( AffineTransform3D transformAB )
	{
		this.transformAB = transformAB;
	}

	@Override
	public AffineTransform3D getTransformationAtoB( int timepointA, int timepointB )
	{
		return transformAB;
	}

	/**
	 * This function takes a collection of pairs of spots. The first spot in each pair
	 * belongs to graphA, the second to graphB. It also returns a collection of pairs
	 * of spots. Where the first spot in each pair is the branch end of the first spot
	 * in the input collection, and the second spot in each pair is the branch end of
	 * the second spot in the input collection.
	 * <p>
	 * A {@link RefRefMap} is used to store the pairs of spots.
	 */
	private static RefRefMap< Spot, Spot > getBranchEnds( RefRefMap< Spot, Spot > pairs, ModelGraph graphA, ModelGraph graphB )
	{
		Spot refA = graphA.vertexRef();
		Spot refB = graphB.vertexRef();
		try
		{
			RefRefMap< Spot, Spot > branchEnds = new RefRefHashMap<>( graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
			RefMapUtils.forEach( pairs, ( spotA, spotB ) -> {
				Spot endA = BranchGraphUtils.getBranchEnd( spotA, refA );
				Spot endB = BranchGraphUtils.getBranchEnd( spotB, refB );
				branchEnds.put( endA, endB );
			} );
			return branchEnds;
		}
		finally
		{
			graphA.releaseRef( refA );
			graphB.releaseRef( refB );
		}
	}
}
