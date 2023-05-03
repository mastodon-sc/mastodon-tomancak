package org.mastodon.mamut.tomancak.spots;

import java.util.List;

import javax.swing.JOptionPane;

import org.mastodon.collection.RefSet;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

public class AddCenterSpots
{
	private AddCenterSpots()
	{
		// prevent from instantiation
	}

	public static void addSpots( MamutAppModel appModel )
	{
		RefSet< Spot > selectedSpots = appModel.getSelectionModel().getSelectedVertices();
		if ( selectedSpots.isEmpty() )
			JOptionPane.showMessageDialog( null, "No spots selected." );
		int numTimepoints = appModel.getMaxTimepoint() + 1;
		List< double[] > averagePosition = SortTreeUtils.calculateAveragePosition( numTimepoints, selectedSpots );
		addSpots( appModel, averagePosition );
	}

	private static void addSpots( MamutAppModel appModel, List< double[] > averagePosition )
	{
		ModelGraph graph = appModel.getModel().getGraph();
		Spot ref = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		Link eref = graph.edgeRef();
		try
		{
			double radius = 10;
			Spot previousSpot = null;
			for ( int timepoint = 0; timepoint < averagePosition.size(); timepoint++ )
			{
				double[] center = averagePosition.get( timepoint );

				if ( center == null )
					continue;

				Spot newSpot = graph.addVertex( ref );
				newSpot.init( timepoint, center, radius );

				if ( previousSpot == null )
					previousSpot = ref2;
				else
					graph.addEdge( previousSpot, newSpot, eref ).init();

				previousSpot.refTo( newSpot );
			}
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
			graph.releaseRef( eref );
		}
	}

}
