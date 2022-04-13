package org.mastodon.mamut.tomancak.compact_lineage;

import bdv.viewer.OverlayRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class CompactLineageRenderer implements OverlayRenderer {

	private List<CompactLineageTree> trees;
	private final double w = 30;
	private final double h = 20;
	private final double radius = 10;

	private final Font font = new Font("Dialog", Font.PLAIN, 12);

	private String focusedSpot;

	public CompactLineageRenderer(List<CompactLineageTree> trees) {
		this.trees= trees;
	}

	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(2.0f));
		drawTrees(g, trees);
	}

	private void drawTrees(Graphics2D g, List<CompactLineageTree> trees) {
		double x = 0;
		for(CompactLineageTree tree: trees) {
			TreeDrawInfo result = drawTree(g, point(x, 0), tree);
			x += result.size.getX();
		}
	}

	public TreeDrawInfo drawTree(Graphics2D g, Point2D topLeft, CompactLineageTree tree) {
		if(tree.isLeaf())
			return drawLeaf(g, topLeft, tree);
		else
			return drawNode(g, topLeft, tree);
	}

	private TreeDrawInfo drawNode(Graphics2D g, Point2D topLeft,
		CompactLineageTree tree)
	{
		boolean focused = tree.getName().equals(focusedSpot);
		TreeDrawInfo a =
			drawTree(g, add(topLeft, point(2 , 2 * h)), tree.getLeft());
		TreeDrawInfo b =
			drawTree(g, add(topLeft, point(  a.size.getX(), 2 * h)), tree.getRight());
		Point2D p = calculateParentNodeLocation(topLeft, a, b);
		drawNode(g, p, focused);
		double maximumTextWidth = b.node.getX() - a.node.getX();
		drawNodeLabel(g, p, tree.getName(), maximumTextWidth);
		double width = a.size.getX() + b.size.getX();
		double height = 2 * h + Math.max(a.size.getY(), b.size.getY());
		drawEdge(g, p, a.node);
		drawEdge(g, p, b.node);
		return new TreeDrawInfo(p, point(width, height));
	}

	private TreeDrawInfo drawLeaf(Graphics2D g, Point2D topLeft,
		CompactLineageTree tree)
	{
		boolean focused = tree.getName().equals(focusedSpot);
		Point2D p = add(topLeft, point(w, h));
		drawNode(g, p, focused);
		drawNodeLabel(g, p, tree.getName(), 2 * w);
		return new TreeDrawInfo(p, point(2*w, 2*h));
	}

	private Point2D calculateParentNodeLocation(Point2D topLeft,
		TreeDrawInfo a,
		TreeDrawInfo b)
	{
		double x = (a.node.getX() + b.node.getX()) * 0.5;
		double y = topLeft.getY() + h;
		return point(x, y);
	}

	private Point2D add(Point2D a, Point2D b) {
		return point(a.getX() + b.getX(), a.getY() + b.getY());
	}

	private Point2D point(double x, double y) {
		return new Point2D.Double(x, y);
	}

	private void drawEdge(Graphics2D g, Point2D parent, Point2D child) {
		g.setColor(Color.gray);
		double x1_offset = child.getX() > parent.getX() ? radius : - radius;
		g.draw(new Line2D.Double(parent.getX() + x1_offset, parent.getY(), child.getX(), parent.getY()));
		g.draw(new Line2D.Double(child.getX(), parent.getY(), child.getX(), child.getY() - radius));
	}

	private void drawNodeLabel(Graphics2D g, Point2D center, String label, double width) {
		g.setFont(scaleFontToFit(label, width, g, font));
		FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D bounds = fontMetrics.getStringBounds(label, g);
		g.drawString(label, (float) (center.getX() - bounds.getWidth() * 0.5),
			(float) (center.getY() + radius + bounds.getHeight()));
	}

	public static Font scaleFontToFit(String text, double width, Graphics g, Font pFont)
	{
		float fontSize = pFont.getSize();
		float text_width = g.getFontMetrics(pFont).stringWidth(text);
		if(text_width <= width)
			return pFont;
		double scaled_font_size = (width / text_width) * fontSize;
		return pFont.deriveFont((float) scaled_font_size);
	}

	private void drawNode(Graphics2D g, Point2D center, boolean focused) {
		Shape shape = new Ellipse2D.Double(
			center.getX() - radius, center.getY() - radius,2 * radius,2 *
			radius);
		g.setColor(Color.lightGray);
		g.fill(shape);
		g.setColor(Color.gray);
		g.draw(shape);
		if(focused) {
			drawFocus(g, center);
		}
	}

	private void drawFocus(Graphics2D g, Point2D center) {
		g.setColor(Color.GREEN);
		double r = radius + 3;
		Rectangle2D.Double s =
			new Rectangle2D.Double(center.getX() - r, center.getY() - r,
				2 * r, 2 * r);
		g.draw(s);
	}

	@Override
	public void drawOverlays(Graphics g) {
		paint(g);
	}

	@Override
	public void setCanvasSize(int width, int height) {

	}

	public void setTrees(List<CompactLineageTree> trees) {
		this.trees = trees;
	}

	public void setFocusedSpot(String spot) {
		this.focusedSpot = spot;
	}

	private class TreeDrawInfo {
		public final Point2D node;
		public final Point2D size;

		public TreeDrawInfo(Point2D parentNodeLocation, Point2D size) {
			this.node = parentNodeLocation;
			this.size = size;
		}
	}
}
