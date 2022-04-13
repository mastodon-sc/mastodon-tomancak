package org.mastodon.mamut.tomancak.compact_lineage;


import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.Arrays;

import javax.swing.*;

/** A demonstration of anti-aliasing */
public class CompactLineageRendererExample extends JPanel{

	private final CompactLineageTree
		treeA = CompactLineageTreeUtil.symmetricTree("2d", 5);

	private final CompactLineageTree
		treeB = CompactLineageTreeUtil.asymmetricTree("3d", 5);

	private final CompactLineageRenderer
		graphRenderer = new CompactLineageRenderer(
		Arrays.asList(treeA, treeB));

	public CompactLineageRendererExample() {
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				repaint();
			}
		});
	}

	/** Draw the example */
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		drawBackground(g);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.scale(0.5, 0.5);
		g.setTransform(affineTransform);
		graphRenderer.paint(g);
	}

	private void drawBackground(Graphics2D g) {
		g.setColor(Color.white)	;
		g.fillRect(0,0,getWidth(), getHeight());
	}

	public static void main(String[] a) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.add(new CompactLineageRendererExample());
		f.setSize(400,400);
		f.setVisible(true);
	}

}
