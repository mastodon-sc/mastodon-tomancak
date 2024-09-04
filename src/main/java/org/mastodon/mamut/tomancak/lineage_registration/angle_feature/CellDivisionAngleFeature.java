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

	public static final String INFO_STRING = "Result of the spatial track matching algorithm: angle between paired cell division directions.";

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
