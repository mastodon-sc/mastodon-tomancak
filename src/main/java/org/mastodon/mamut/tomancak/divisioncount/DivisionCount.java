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

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.util.TreeUtils;

public class DivisionCount
{
	private DivisionCount()
	{
		// prevent instantiation
	}

	public static List< Pair< Integer, Integer > > getTimepointAndDivisions( final Model model )
	{
		int minTimepoint = TreeUtils.getMinTimepoint( model );
		int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		List< Pair< Integer, Integer > > timepointAndDivisions = new ArrayList<>();
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			int divisions = 0;
			for ( Spot spot : model.getSpatioTemporalIndex().getSpatialIndex( timepoint ) )
			{
				if ( spot.outgoingEdges().size() > 1 )
					divisions++;
			}
			timepointAndDivisions.add( Pair.of( timepoint, divisions ) );
		}
		return timepointAndDivisions;
	}
}
