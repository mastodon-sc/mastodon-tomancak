package org.mastodon.mamut.tomancak.compact_lineage;

import bdv.viewer.InteractiveDisplayCanvas;
import net.miginfocom.swing.MigLayout;
import org.mastodon.graph.ref.IncomingEdges;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.FocusModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CompactLineagePanel extends JPanel {

	private final InteractiveDisplayCanvas canvas;
	private final CompactLineageRenderer renderer;
	private LineageComboBox treeComboBox;
	private final FocusModel<Spot, Link> focusModel;
	private final ModelGraph graph;

	CompactLineagePanel(MamutAppModel appModel) {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 300));
		Model model = appModel.getModel();
		graph = model.getGraph();
		List<CompactLineageTree> trees = CompactLineageTreeUtil.createTrees(graph);
		focusModel = appModel.getFocusModel();
		focusModel.listeners().add(this::focusChanged);
		renderer = new CompactLineageRenderer(trees);
		canvas = new InteractiveDisplayCanvas(getWidth(), getHeight());
		canvas.overlays().add(renderer);
		add(canvas);
		add(initTopPanel(trees), BorderLayout.PAGE_START);
//		JScrollBar verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);
//		add(verticalScrollBar, BorderLayout.LINE_END);
//		JScrollBar horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
//		add(horizontalScrollBar, BorderLayout.PAGE_END);
	}

	private void focusChanged() {
		final Spot spot = focusModel.getFocusedVertex(graph.vertexRef());
		getParent(spot);
		renderer.setFocusedSpot(spot == null ? null : spot.getLabel());
		canvas.repaint();
	}

	private void getParent(Spot spot) {
		if(spot == null)
			return;
		Link link = graph.edgeRef();
		Spot parent = graph.vertexRef();
		while(true) {
			IncomingEdges<Link> links = spot.incomingEdges();
			if(links.size() == 0)
				break;
			link = links.get(0, link);
			parent = link.getSource(parent);
			if(parent.outgoingEdges().size() > 1)
				break;
			spot.refTo(parent);
		}
		graph.releaseRef(link);
		graph.releaseRef(parent);
	}

	private JPanel initTopPanel(List<CompactLineageTree> trees) {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new MigLayout());
		topPanel.add(new JLabel("Select lineage:"));
		treeComboBox = initTreeComboBox(trees);
		topPanel.add(treeComboBox);
		return topPanel;
	}

	private LineageComboBox initTreeComboBox(List<CompactLineageTree> trees) {
		final LineageComboBox comboBoc = new LineageComboBox(trees);
		comboBoc.selectionChangedListeners().add(this::onSelectedTreeChanged);
		return comboBoc;
	}

	private void onSelectedTreeChanged(CompactLineageTree tree) {
		renderer.setTrees(Collections.singletonList(tree));
		canvas.repaint();
	}
}
