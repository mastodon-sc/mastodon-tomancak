package org.mastodon.mamut.tomancak.label_systematically;

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collections;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class LabelSpotsSystematicallyTest
{
	@Test
	public void test() {
		ModelGraph graph = new ModelGraph();
		Spot a = addSpot(graph, "a");
		Spot a1 = addSpotAsDescendantOf(graph, "", a);
		Spot a2 = addSpotAsDescendantOf(graph, "", a);
		LabelSpotsSystematically.setLabels(graph, spot -> true, new TrackSchemeOrder() );
		assertEquals("a1", a1.getLabel());
		assertEquals("a2", a2.getLabel());
	}

	@Test
	public void testRenameUnnamedBranches() {
		ModelGraph graph = new ModelGraph();
		Spot a = addSpot(graph, "a");
		Spot named = addSpotAsDescendantOf(graph, "cellname", a);
		Spot noname = addSpotAsDescendantOf(graph, "239847", a);
		BranchFilter filter = new BranchFilter( graph );
		filter.setMatchUnnamed( true );
		LabelSpotsSystematically.setLabels(graph, filter, new TrackSchemeOrder() );
		assertEquals("cellname", named.getLabel());
		assertEquals("a2", noname.getLabel());
	}

	@Test
	public void testRenameCellsEndingWithOneOrTwo() {
		ModelGraph graph = new ModelGraph();
		Spot a = addSpot(graph, "a" );
		Spot named = addSpotAsDescendantOf(graph, "cell2", a);
		Spot noname = addSpotAsDescendantOf(graph, "cell7", a);
		BranchFilter spotPredicate = new BranchFilter( graph );
		spotPredicate.setLabelEndsWith1or2Filter();
		Predicate<Spot> correctOrder = spot -> true;
		LabelSpotsSystematically.setLabels(graph, spotPredicate, correctOrder );
		assertEquals("a1", named.getLabel());
		assertEquals("cell7", noname.getLabel());
	}

	private double[] array( double... values )
	{
		return values;
	}

	@Test
	public void testRenameCellsAsInternExtern() {
		ModelGraph graph = new ModelGraph();
		Spot a = addSpot(graph, "a", array(2, 2, 2));
		Spot a2 = addSpotAsDescendantOf(graph, "1", a, array(1, 2, 2)); // extern
		Spot a1 = addSpotAsDescendantOf(graph, "2", a, array(3, 2, 2)); // intern
		Spot center = addSpot(graph, "center", array(4, 2, 2));
		Spot b = addSpot(graph, "b", array(6, 2, 2));
		Spot b1 = addSpotAsDescendantOf(graph, "3", b, array(5, 2, 2)); // intern
		Spot b2 = addSpotAsDescendantOf(graph, "4", b, array(7, 2, 2)); // extern
		BranchFilter branchFilter = new BranchFilter( graph );
		branchFilter.setMatchUnnamed( true );
		LabelSpotsSystematically.setLabels( graph, branchFilter, new InternExternOrder( graph, Collections.singleton(center) ) );
		assertEquals("a1", a1.getLabel());
		assertEquals("a2", a2.getLabel());
		assertEquals("b1", b1.getLabel());
		assertEquals("b2", b2.getLabel());
	}

	//	Handling:
//	• Chose center position, let it run
//    • Which cells:
//		◦ Renames all cells with no name
//        ◦ Descendants of selected cells
//        ◦ Selected cells
//        ◦ [1-9][a-dA-D][12]*
//		◦ Cells that have the same name as their parent but add a 1 or 2
//		◦ Cell names that end with 1 or 2
//	• Nomenclature:
//		◦ left anchor, right anchor
//        ◦ Same as TrackScheme
//        ◦ Interior / Exterior

	private Spot addSpot( ModelGraph graph, String label )
	{
		return addSpot( graph, label, new double[] { 0, 0, 0 } );
	}

	private Spot addSpot( ModelGraph graph, String label, double[] position )
	{
		Spot spot = graph.addVertex();
		spot.init( 0, position, 1 );
		spot.setLabel( label );
		return spot;
	}

	private Spot addSpotAsDescendantOf( ModelGraph graph, String label, Spot parent )
	{
		return addSpotAsDescendantOf( graph, label, parent, new double[] { 0, 0, 0 } );
	}

	private Spot addSpotAsDescendantOf( ModelGraph graph, String label, Spot parent, double[] position )
	{
		Spot spot = graph.addVertex();
		spot.init( spot.getTimepoint() + 1, position, 1 );
		spot.setLabel( label );
		graph.releaseRef( graph.addEdge( parent, spot ).init() );
		return spot;
	}

}
