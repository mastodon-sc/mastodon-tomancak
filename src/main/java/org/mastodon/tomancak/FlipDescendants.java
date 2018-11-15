package org.mastodon.tomancak;

import org.mastodon.graph.ref.OutgoingEdges;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

public class FlipDescendants
{
	public static void flipDescendants( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();
		Spot spot = appModel.getFocusModel().getFocusedVertex( graph.vertexRef() );
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
