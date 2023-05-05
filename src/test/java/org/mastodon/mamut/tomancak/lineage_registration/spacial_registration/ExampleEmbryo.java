package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Holds a {@link Model} that is used in the {@link DynamicLandmarkRegistrationTest}.
 * <p>
 * The graph has 3 spot at timepoint 0: a, b, c. All three spots immediately
 * divide into a1, a2, b1, b2, c1, c2. Only c2 divides further into c21 and
 * c22.
 */
public class ExampleEmbryo
{

	final Model model = new Model();

	final ModelGraph graph = model.getGraph();

	final Spot a, a1, a1End, a2, a2End, b, b1, b1End, b2, b2End, c, c1, c1End, c2, c2End, c21, c22;

	ExampleEmbryo()
	{
		a = addSpot( graph, "A", 0, 2, 2, 0 );
		a1 = addSpot( graph, "A1", a, 2, 1, 0 );
		a1End = addBranch( graph, a1, 5 );
		a2 = addSpot( graph, "A2", a, 2, 3, 0 );
		a2End = addBranch( graph, a2, 5 );
		b = addSpot( graph, "B", 0, 4, 2, 0 );
		b1 = addSpot( graph, "B1", b, 4, 1, 0 );
		b1End = addBranch( graph, b1, 5 );
		b2 = addSpot( graph, "B2", b, 4, 3, 0 );
		b2End = addBranch( graph, b2, 5 );
		c = addSpot( graph, "C", 0, 4, 4, 0 );
		c1 = addSpot( graph, "C1", c, 4, 4, 0 );
		c1End = addBranch( graph, c1, 5 );
		c2 = addSpot( graph, "C2", c, 4, 5, 0 );
		c2End = addBranch( graph, c2, 4 );
		c21 = addSpot( graph, "C21", c2End, 3, 5, 0 );
		c22 = addSpot( graph, "C22", c2End, 5, 5, 0 );
	}

	// helper methods

	private static Spot addSpot( ModelGraph graph, String label, int time, double... position )
	{
		Spot spot = graph.addVertex().init( time, position, 1 );
		spot.setLabel( label );
		return spot;
	}

	static Spot addSpot( ModelGraph graph, String label, Spot parent, double... position )
	{
		int time = parent == null ? 2 : parent.getTimepoint() + 1;
		Spot spot = addSpot( graph, label, time, position );
		if ( parent != null )
			graph.addEdge( parent, spot );
		return spot;
	}

	static Spot addBranch( ModelGraph graph, Spot branchStart, int length )
	{
		String label = branchStart.getLabel();
		double[] position = { branchStart.getDoublePosition( 0 ), branchStart.getDoublePosition( 1 ), branchStart.getDoublePosition( 2 ) };
		Spot s = branchStart;
		for ( int i = 1; i < length; i++ )
			s = addSpot( graph, label + "~" + i, s, position );
		return s;
	}
}
