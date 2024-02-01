/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.compact_lineage;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.graph.ref.OutgoingEdges;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.ArrayList;
import java.util.List;

public class CompactLineageTreeUtil {

	/**
	 * Create a {@link CompactLineageTree} for a mastodon {@link ModelGraph}.
	 */
	public static List<CompactLineageTree> createTrees(ModelGraph graph) {
		RefArrayList<Spot> roots = getRoots(graph);
		List<CompactLineageTree> trees = createTrees(roots);
		return trees;
	}

	private static RefArrayList<Spot> getRoots(ModelGraph graph) {
		RefArrayList<Spot> roots = new RefArrayList<>(graph.vertices() .getRefPool());
		for(Spot spot : graph.vertices())
			if (spot.incomingEdges().isEmpty())
				roots.add(spot);
		return roots;
	}

	private static List<CompactLineageTree> createTrees(RefArrayList<Spot> roots) {
		List<CompactLineageTree> trees = new ArrayList<>();
		for(Spot root : roots)
			trees.add(createTree(root));
		return trees;
	}

	private static CompactLineageTree createTree(Spot root) {
		Spot branchEnd = branchEnd(root);
		if(branchEnd == null)
			return CompactLineageTree.leaf(root.getLabel());
		OutgoingEdges<Link> edges = branchEnd.outgoingEdges();
		Spot a = edges.get(0).getTarget();
		CompactLineageTree treeA = createTree(a);
		Spot b = edges.get(1).getTarget();
		CompactLineageTree treeB = createTree(b);
		return CompactLineageTree.node(root.getLabel(), treeA, treeB);
	}

	private static Spot branchEnd(Spot root) {
		// TODO avoid unnecessary object creation
		Spot branchEnd = root.getModelGraph().vertexRef();
		branchEnd.refTo(root);
		while(true) {
			OutgoingEdges<Link> edges = branchEnd.outgoingEdges();
			if(edges.isEmpty()) {
				branchEnd = null;
				break;
			}
			if(edges.size() > 1)
				break;
			Link link = edges.get(0);
			branchEnd = link.getTarget(branchEnd);
		}
		return branchEnd;
	}

	/** For testing. Create a symmetric lineage tree. */
	public static CompactLineageTree symmetricTree(String nodeLabel, int depth) {
		if(depth <= 1)
			return CompactLineageTree.leaf(nodeLabel);
		else
			return CompactLineageTree.node(nodeLabel,
				symmetricTree(nodeLabel + "1", depth - 1),
				symmetricTree(nodeLabel + "2", depth - 1));
	}

	/** For testing: Create a asymmetric lineage tree. */
	public static CompactLineageTree asymmetricTree(String nodeLabel, int depth) {
		if(depth <= 1)
			return CompactLineageTree.leaf(nodeLabel);
		else
			return CompactLineageTree.node(nodeLabel,
				asymmetricTree(nodeLabel + "1", depth - 1),
				CompactLineageTree.leaf(nodeLabel + "2"));
	}

}
