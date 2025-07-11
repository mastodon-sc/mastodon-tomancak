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
package org.mastodon.mamut.tomancak.divisioncount;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;
import org.mastodon.util.TreeUtils;

public class SpotAndDivisionCount
{
	private SpotAndDivisionCount()
	{
		// prevent instantiation
	}

	/**
	 * Calculates the number of spots and divisions per time point in the given model.
	 *
	 * @param projectModel The project model containing the spots and edges.
	 * @param onlySelectedSpots If {@code true}, only counts spots that are selected.
	 * @return A list of triples, where each triple contains (in that order) the timepoint, the number of spots at that timepoint, and the number of divisions at that timepoint.
	 */
	public static List< Triple< Integer, Integer, Integer > > getSpotAndDivisionsPerTimepoint( final ProjectModel projectModel,
			final boolean onlySelectedSpots )
	{
		final Model model = projectModel.getModel();
		final SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
		int minTimepoint = TreeUtils.getMinTimepoint( model );
		int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		List< Triple< Integer, Integer, Integer > > timepointAndDivisions = new ArrayList<>();
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			int spots = 0;
			int divisions = 0;
			for ( Spot spot : model.getSpatioTemporalIndex().getSpatialIndex( timepoint ) )
			{
				if ( onlySelectedSpots && !selectionModel.isSelected( spot ) )
					continue;
				if ( spot.outgoingEdges().size() > 1 )
					divisions++;
				if ( onlySelectedSpots )
					spots++;
			}
			if ( !onlySelectedSpots )
				spots = model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size();
			timepointAndDivisions.add( Triple.of( timepoint, spots, divisions ) );
		}
		return timepointAndDivisions;
	}
}
