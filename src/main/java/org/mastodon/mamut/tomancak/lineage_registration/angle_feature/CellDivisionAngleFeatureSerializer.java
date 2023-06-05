package org.mastodon.mamut.tomancak.lineage_registration.angle_feature;

import org.mastodon.feature.DoubleScalarFeatureSerializer;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class CellDivisionAngleFeatureSerializer extends DoubleScalarFeatureSerializer< CellDivisionAngleFeature, Spot >
{

	@Override
	public FeatureSpec< CellDivisionAngleFeature, Spot > getFeatureSpec()
	{
		return CellDivisionAngleFeature.SPEC;
	}
}
