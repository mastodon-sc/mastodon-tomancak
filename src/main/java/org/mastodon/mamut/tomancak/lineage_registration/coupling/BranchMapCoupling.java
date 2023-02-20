package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.BranchGraphUtils;
import org.mastodon.mamut.tomancak.lineage_registration.RegisteredGraphs;

/**
 * <p>
 * This class couples a source {@link SpotHook} to a target {@link SpotHook}.
 * </p>
 * <p>
 * {@link BranchMapCoupling} listens to spot changes in the source {@link SpotHook}.
 * When a spot changes, the branch start that belongs to the spot in the source graph
 * is found. The branch start is then mapped to the target graph by using the provided
 * {@link RegisteredGraphs#mapAB}. Finally, the branch start in the target graph is
 * set to the target {@link SpotHook}.
 * </p>
 */
class BranchMapCoupling implements Consumer< Spot >
{
	private final ModelGraph sourceGraph;

	private final ModelGraph targetGraph;

	private final SpotHook targetHook;

	/** Maps branch starts in source graph to branch starts in target graph. */
	private final RefRefMap< Spot, Spot > map;

	public BranchMapCoupling(
			SpotHook sourceHook,
			SpotHook targetHook,
			RegisteredGraphs registeredGraphs )
	{
		this.sourceGraph = registeredGraphs.graphA;
		this.targetGraph = registeredGraphs.graphB;
		sourceHook.setListener( this );
		this.targetHook = targetHook;
		this.map = registeredGraphs.mapAB;
	}

	@Override
	public void accept( Spot spotA )
	{
		Spot refA = sourceGraph.vertexRef();
		Spot refB = targetGraph.vertexRef();
		try
		{
			Spot branchStartA = spotA == null ? null : BranchGraphUtils.getBranchStart( spotA, refA );
			Spot spotB = branchStartA == null ? null : map.get( branchStartA, refB );
			targetHook.set( spotB );
		}
		finally
		{
			sourceGraph.releaseRef( refA );
			targetGraph.releaseRef( refB );
		}
	}
}
