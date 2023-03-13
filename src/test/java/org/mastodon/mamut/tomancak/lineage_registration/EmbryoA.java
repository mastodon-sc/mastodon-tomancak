package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Example data for testing {@link LineageRegistrationAlgorithm} and {@link LineageRegistrationUtils}.
 */
class EmbryoA
{

	final Model model = new Model();

	final ModelGraph graph = model.getGraph();

	final Spot a = addSpot( graph, null, "A", 2, 2, 0 );

	final Spot aEnd = addBranch( graph, a, 2 );

	final Spot a1 = addSpot( graph, aEnd, "A1", 2, 1, 0 );

	final Spot a2 = addSpot( graph, aEnd, "A2", 2, 3, 0 );

	final Spot b = addSpot( graph, null, "B", 4, 2, 0 );

	final Spot bEnd = addBranch( graph, b, 3 );

	final Spot b1 = addSpot( graph, bEnd, "B1", 4, 1, 0 );

	final Spot b2 = addSpot( graph, bEnd, "B2", 4, 3, 0 );

	final Spot c = addSpot( graph, null, "C", 4, 4, 0 );

	final Spot c1 = addSpot( graph, c, "C1", 4, 4, 0 );

	final Spot c2 = addSpot( graph, c, "C2", 4, 5, 0 );

	// helper methods

	private static Spot addSpot( ModelGraph graph, Spot parent, String label, double... position )
	{
		int t = parent == null ? 0 : parent.getTimepoint() + 1;
		Spot spot = graph.addVertex().init( t, position, 1 );
		spot.setLabel( label );
		if ( parent != null )
			graph.addEdge( parent, spot );
		return spot;
	}

	private static Spot addBranch( ModelGraph graph, Spot branchStart, int length )
	{
		String label = branchStart.getLabel();
		double[] position = { branchStart.getDoublePosition( 0 ), branchStart.getDoublePosition( 1 ), branchStart.getDoublePosition( 2 ) };
		Spot s = branchStart;
		for ( int i = 1; i < length; i++ )
			s = addSpot( graph, s, label + "~" + i, position );
		return s;
	}
}
