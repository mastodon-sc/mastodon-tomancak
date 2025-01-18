/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.trackmatching.coupling;

import java.util.function.Consumer;

import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.trackmatching.BranchGraphUtils;
import org.mastodon.mamut.tomancak.trackmatching.RegisteredGraphs;

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
