package org.mastodon.mamut.tomancak.lineage_registration.angle_feature;

import java.util.Iterator;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.DoubleScalarFeature;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class CellDivisionAngleFeature extends DoubleScalarFeature< Spot >
{

	private static final String KEY = "Cell division angle";

	public static final String INFO_STRING = "Result of the lineage registration algorithm: angle between paired cell division directions.";

	public static final FeatureProjectionSpec PROJECTION_SPEC =
			new FeatureProjectionSpec( KEY, Dimension.ANGLE );

	public static final Spec SPEC = new Spec();

	/**
	 * Adds a {@link CellDivisionAngleFeature} to the {@code model.getFeatureModel()}.
	 *
	 * @param model The {@link Model} to which the feature is added.
	 * @param angles The values that will be stored in the feature.
	 *               This {@link RefDoubleMap} is expected to only contain values
	 *               for the first spot of each branch. This value is copied to all
	 *               other spots of the branch.
	 */
	public static void declare( Model model, RefDoubleMap< Spot > angles )
	{
		DoublePropertyMap< Spot > spotAngles = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		ModelBranchGraph branchGraph = model.getBranchGraph();
		BranchSpot ref = branchGraph.vertexRef();
		for ( Spot spot : angles.keySet() )
		{
			double angle = angles.get( spot );
			BranchSpot branchSpot = branchGraph.getBranchVertex( spot, ref );
			Iterator< Spot > iterator = branchGraph.vertexBranchIterator( branchSpot );
			while ( iterator.hasNext() )
				spotAngles.set( iterator.next(), angle );
			branchGraph.releaseIterator( iterator );
		}
		branchGraph.releaseRef( ref );

		CellDivisionAngleFeature feature = new CellDivisionAngleFeature( spotAngles );
		model.getFeatureModel().declareFeature( feature );
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< CellDivisionAngleFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					CellDivisionAngleFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public CellDivisionAngleFeature( DoublePropertyMap< Spot > angles )
	{
		super( KEY, Dimension.ANGLE, "degree", angles);
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return SPEC;
	}
}
