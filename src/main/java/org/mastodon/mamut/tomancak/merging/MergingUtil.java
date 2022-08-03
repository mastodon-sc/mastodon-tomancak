/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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
	 * Returns <code>true</code> if {@code spot} has a set label (vs label
	 * generated from id).
	 * 
	 * @param spot
	 *            the spot.
	 * @return <code>true</code> if {@code spot} has a set label
	 */
	public static boolean hasLabel( final Spot spot )
	{
		final SpotPool pool = ( SpotPool ) spot.getModelGraph().vertices().getRefPool();
		@SuppressWarnings( "unchecked" )
		final ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
		return labels.isSet( spot );
	}

	/**
	 * Returns number of time-points in {@code project}. To to that, loads
	 * {@code spimdata} for {@code project}.
	 * 
	 * @param project
	 *            the project.
	 * @return the number of time-points in the project.
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
	 * 
	 * @param model
	 *            the model.
	 * @param numTimepoints
	 *            the number of time-points in the model.
	 * @return the largest timepoint (index) where model has a least one spot.
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
