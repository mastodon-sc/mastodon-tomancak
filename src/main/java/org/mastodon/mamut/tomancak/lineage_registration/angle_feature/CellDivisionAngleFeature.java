package org.mastodon.mamut.tomancak.lineage_registration.angle_feature;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.plugin.Plugin;

public class CellDivisionAngleFeature extends BranchScalarFeature< BranchSpot, Spot >
{

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< CellDivisionAngleFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					CellDivisionAngleFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public static final String INFO_STRING = "Result of the lineage registration algorithm: angle between paired cell division directions.";

	private static final String KEY = "Angle between paired cell divisions";

	private static final Dimension DIMENSION = Dimension.ANGLE;

	public static final FeatureProjectionSpec PROJECTION_SPEC =
			new FeatureProjectionSpec( KEY, DIMENSION );

	private static final Spec specs = new Spec();

	public CellDivisionAngleFeature( final Model model )
	{
		super( KEY, DIMENSION, DIMENSION.getUnits( model.getSpaceUnits(), model.getTimeUnits() ),
				model.getBranchGraph(), model.getGraph().vertices().getRefPool() );
	}

	@Override
	public Spec getSpec()
	{
		return specs;
	}
}
