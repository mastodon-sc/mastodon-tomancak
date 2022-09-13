package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class LabelSpotsSystematically
{

	public static void setLabelsBasedOnInternExtern( Collection<Spot> center, ModelGraph graph, boolean renameUnnamed, boolean renameLabelsEndingWith1Or2 )
	{
		BranchFilter branchFilter = new BranchFilter( graph );
		branchFilter.setMatchUnnamed( renameUnnamed );
		if ( renameLabelsEndingWith1Or2 )
			branchFilter.setLabelFilter(label -> label.endsWith( "1" ) || label.endsWith( "2" ));
		setLabels( graph, branchFilter, new InternExternOrder( graph, center ) );
	}

	public static void setLabels( ModelGraph graph, Predicate<Spot> filter, Predicate<Spot> correctOrder )
	{
		for(Spot root : graph.vertices())
			if(root.incomingEdges().isEmpty())
				renameDescendants( graph, filter, correctOrder, null, root, true );
	}

	private static void renameDescendants( ModelGraph graph, Predicate<Spot> filter, Predicate<Spot> correctOrder, Spot parent, Spot spot, boolean first )
	{
		Spot ref1 = graph.vertexRef();
		Spot ref2 = graph.vertexRef();
		Spot ref3 = graph.vertexRef();
		try {
			if( parent != null && filter.test( spot ) )
				renameBranch( graph, spot, parent.getLabel() + (first ? "1" : "2") );
			Spot branchEnd = getBranchEnd( spot, ref1 );
			if(branchEnd.outgoingEdges().size() != 2)
				return;
			Iterator<Link> edges = branchEnd.outgoingEdges().iterator();
			Spot child1 = edges.next().getTarget(ref2);
			Spot child2 = edges.next().getTarget(ref3);
			boolean b = correctOrder.test( branchEnd );
			renameDescendants(graph, filter, correctOrder, spot, child1, b );
			renameDescendants(graph, filter, correctOrder, spot, child2, !b );
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
			graph.releaseRef( ref3 );
		}
	}

	private static void renameBranch( ModelGraph graph, Spot spot, String label )
	{
		Spot ref = graph.vertexRef();
		try {
			Spot s = spot;
			s.setLabel( label );
			while( s.outgoingEdges().size() == 1)
			{
				s = s.outgoingEdges().iterator().next().getTarget( ref );
				s.setLabel( label );
			}
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}

	private static Spot getBranchEnd( Spot spot, Spot ref )
	{
		Spot last = spot;
		while( last.outgoingEdges().size() == 1)
			last = last.outgoingEdges().iterator().next().getTarget( ref );
		return last;
	}
}
