/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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
