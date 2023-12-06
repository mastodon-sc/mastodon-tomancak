/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.spots;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.mastodon.model.SelectionModel;

public class AddCenterSpots
{
	private AddCenterSpots()
	{
		// prevent from instantiation
	}

	public static void addSpots( ProjectModel appModel )
	{
		RefSet< Spot > selectedSpots = appModel.getSelectionModel().getSelectedVertices();
		if ( selectedSpots.isEmpty() )
		{
			JOptionPane.showMessageDialog( null, "No spots selected." );
			return;
		}

		Model model = appModel.getModel();
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.WriteLock writeLock = graph.getLock().writeLock();
		writeLock.lock();
		try
		{
			int numTimepoints = appModel.getMaxTimepoint() + 1;
			List< double[] > averagePosition = SortTreeUtils.calculateAveragePosition( numTimepoints, selectedSpots );
			Pair< RefList< Spot >, RefList< Link > > newSpotsAndLinks = addSpots( graph, averagePosition );
			graph.notifyGraphChanged();
			model.setUndoPoint();
			selectSpotsAndLinks( appModel, newSpotsAndLinks.getLeft(), newSpotsAndLinks.getRight() );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private static Pair< RefList< Spot >, RefList< Link > > addSpots( ModelGraph graph, List< double[] > averagePosition )
	{
		RefList< Spot > newSpots = new RefArrayList<>( graph.vertices().getRefPool() );
		RefList< Link > newLinks = new RefArrayList<>( graph.edges().getRefPool() );
		Spot ref = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		Link eref = graph.edgeRef();
		try
		{
			double radius = 10;
			for ( int timepoint = 0; timepoint < averagePosition.size(); timepoint++ )
			{
				double[] center = averagePosition.get( timepoint );
				if ( center != null )
				{
					Spot spot = graph.addVertex( ref ).init( timepoint, center, radius );
					newSpots.add( spot );
				}
			}

			for ( int i = 0; i < newSpots.size() - 1; i++ )
			{
				Link link = graph.addEdge( newSpots.get( i, ref ), newSpots.get( i + 1, ref2 ), eref ).init();
				newLinks.add( link );
			}
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
			graph.releaseRef( eref );
		}
		return Pair.of( newSpots, newLinks );
	}

	private static void selectSpotsAndLinks( ProjectModel appModel, RefList< Spot> spots, RefList< Link> links )
	{
		SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();
		selectionModel.pauseListeners();
		selectionModel.clearSelection();
		selectionModel.setVerticesSelected( spots, true );
		selectionModel.setEdgesSelected( links, true );
		selectionModel.resumeListeners();
	}
}
