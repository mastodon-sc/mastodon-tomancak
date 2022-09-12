package org.mastodon.mamut.tomancak.sort_tree;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SortTreeInternExternTest
{
	@Test
	public void test() {
		Model model = new Model();
		// example graph
		ModelGraph graph = model.getGraph();
		Spot a = graph.addVertex().init( 0, array( 3, 5, 5), 1 );
		Spot aIntern = graph.addVertex().init( 1, array( 4, 5, 5 ), 1 );
		Spot aExtern = graph.addVertex().init( 1, array( 2, 5, 5 ), 1 );
		Spot b = graph.addVertex().init( 0, array( 7, 5, 5), 1 );
		Spot bIntern = graph.addVertex().init( 1, array( 6, 5, 5 ), 1 );
		Spot bExtern = graph.addVertex().init( 1, array( 8, 5, 5 ), 1 );
		graph.addEdge( a, aExtern ).init();
		graph.addEdge( a, aIntern ).init();
		graph.addEdge( b, bIntern ).init();
		graph.addEdge( b, bExtern ).init();
		// add center marker
		Spot center = graph.addVertex().init( 0, array(5,5,5), 1 );
		// sort
		SortTreeInternExtern.sort(model, graph.vertices(), Collections.singleton(center));
		// assert
		assertEquals(aIntern, a.outgoingEdges().iterator().next().getTarget());
		assertEquals(bIntern, b.outgoingEdges().iterator().next().getTarget());
	}

	private double[] array( double... values )
	{
		return values;
	}
}
