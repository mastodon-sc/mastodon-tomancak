package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.mamut.model.Spot;

/**
 * Example data for testing {@link LineageRegistrationAlgorithm} and {@link LineageRegistrationUtils}.
 * The graph and coordinates are the similar to {@link EmbryoB}, but the graph in this class has
 * additional spots abc, bc and beforeA, that are added to the graph before the first spots of {@link EmbryoB}.
 * <p>
 * {@link EmbryoBSingleCellStage#graph} has therefore only one root node: abc.
 *
 */
class EmbryoBSingleCellStage extends EmbryoB
{

	Spot abc;

	Spot bc;

	Spot beforeA;

	EmbryoBSingleCellStage()
	{
		super( 2 );
		abc = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		bc = graph.addVertex().init( 1, new double[] { 1, 0, 0 }, 1 );
		beforeA = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 1 );
		beforeA.setLabel( "A" );
		graph.addEdge( abc, bc );
		graph.addEdge( abc, beforeA );
		graph.addEdge( beforeA, a );
		graph.addEdge( bc, b );
		graph.addEdge( bc, c );
	}

}
