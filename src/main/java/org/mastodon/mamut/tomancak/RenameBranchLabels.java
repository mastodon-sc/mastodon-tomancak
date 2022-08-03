package org.mastodon.mamut.tomancak;

import javax.swing.JOptionPane;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.model.FocusModel;

public class RenameBranchLabels
{
	public static void run( MamutPluginAppModel pluginAppModel )
	{
		MamutAppModel appModel = pluginAppModel.getAppModel();
		FocusModel<Spot, Link> focusModel = appModel.getFocusModel();
		Model model = appModel.getModel();
		ModelGraph graph = model.getGraph();
		Spot ref = graph.vertexRef();
		try
		{
			Spot focusedSpot = focusModel.getFocusedVertex( ref );
			String newLabel = askForNewLabel( focusedSpot );
			renameBranch( graph, focusedSpot, newLabel );
		}
		finally
		{
			graph.releaseRef( ref);
		}
	}

	private static String askForNewLabel( Spot focusedSpot )
	{
		if(focusedSpot == null) {
			JOptionPane.showMessageDialog( null, "No spot focused.", "Change branch labels", JOptionPane.INFORMATION_MESSAGE );
			return null;
		}
		String oldLabel = focusedSpot.getLabel();
		return (String) JOptionPane.showInputDialog( null, "Spot label:", "Label spots", JOptionPane.PLAIN_MESSAGE, null, null, oldLabel );
	}

	private static void renameBranch( ModelGraph graph, Spot focusedVertex, String newLabel )
	{
		if(focusedVertex == null || newLabel == null)
			return;
		focusedVertex.setLabel( newLabel );
		renameBranchSpotsForward( graph, focusedVertex, newLabel );
		renameBranchSpotsBackward( graph, focusedVertex, newLabel );
	}

	private static void renameBranchSpotsForward( ModelGraph graph, Spot focusedVertex, String newLabel )
	{
		Spot spot = graph.vertexRef();
		try {
			spot.refTo( focusedVertex );
			while( spot.outgoingEdges().size() == 1 ) {
				spot = spot.outgoingEdges().iterator().next().getTarget( spot );
				if ( spot.incomingEdges().size() != 1 )
					break;
				spot.setLabel( newLabel );
			}
		}
		finally {
			graph.releaseRef( spot );
		}
	}

	private static void renameBranchSpotsBackward( ModelGraph graph, Spot focusedVertex, String newLabel )
	{
		Spot spot = graph.vertexRef();
		try {
			spot.refTo( focusedVertex );
			while( spot.incomingEdges().size() == 1 ) {
				spot = spot.incomingEdges().iterator().next().getSource( spot );
				if ( spot.outgoingEdges().size() != 1 )
					break;
				spot.setLabel( newLabel );
			}
		}
		finally {
			graph.releaseRef( spot );
		}
	}

}
