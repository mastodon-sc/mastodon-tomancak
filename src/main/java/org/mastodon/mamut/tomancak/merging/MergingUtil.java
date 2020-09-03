package org.mastodon.mamut.tomancak.merging;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.util.DummySpimData;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

public class MergingUtil
{
	/**
	 * Returns true if {@code spot} has a set label (vs label generated from id).
	 */
	public static boolean hasLabel( final Spot spot )
	{
		final SpotPool pool = ( SpotPool ) spot.getModelGraph().vertices().getRefPool();
		final ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
		return labels.isSet( spot );
	}

	/**
	 * Returns number of timepoints in {@code project}.
	 * To to that, loads spimdata for {@code project}.
	 */
	public static int getNumTimepoints( final MamutProject project )
	{
		try
		{
			final String spimDataXmlFilename = project.getDatasetXmlFile().getAbsolutePath();
			SpimDataMinimal spimData = DummySpimData.tryCreate( project.getDatasetXmlFile().getName() );
			if ( spimData == null )
				spimData = new XmlIoSpimDataMinimal().load( spimDataXmlFilename );
			return spimData.getSequenceDescription().getTimePoints().size();
		}
		catch ( final SpimDataException e )
		{
			throw new RuntimeException( e );
		}
	}

	// Helper: max timepoint that has at least one spot

	/**
	 * Returns the largest timepoint (index) where model has a least one spot.
	 */
	public static int getMaxNonEmptyTimepoint( final Model model, final int numTimepoints )
	{
		int maxNonEmptyTimepoint = 0;
		final SpatioTemporalIndex< Spot > spatioTemporalIndex = model.getSpatioTemporalIndex();
		spatioTemporalIndex.readLock().lock();
		try
		{
			for ( int t = 0; t < numTimepoints; ++t )
			{
				final SpatialIndex< Spot > index = spatioTemporalIndex.getSpatialIndex( t );
				if ( index.size() > 0 )
					maxNonEmptyTimepoint = t;
			}
		}
		finally
		{
			spatioTemporalIndex.readLock().unlock();
		}
		return maxNonEmptyTimepoint;
	}
}
