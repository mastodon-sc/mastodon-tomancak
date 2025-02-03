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
