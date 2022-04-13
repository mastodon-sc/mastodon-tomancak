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
