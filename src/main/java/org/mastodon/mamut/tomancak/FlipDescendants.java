package org.mastodon.mamut.tomancak;

import org.mastodon.graph.ref.OutgoingEdges;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class FlipDescendants
{
	public static void flipDescendants( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		final Spot spot = appModel.getFocusModel().getFocusedVertex( graph.vertexRef() );
		final OutgoingEdges< Link > outgoing = spot.outgoingEdges();
		if ( outgoing.size() > 1 )
		{
			final Link first = outgoing.get( 0 );
			final Spot target = first.getTarget();
			graph.remove( first );
			graph.addEdge( spot, target ).init();
			graph.notifyGraphChanged();
		}
	}
}
