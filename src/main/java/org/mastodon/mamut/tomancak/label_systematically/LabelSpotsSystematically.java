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
package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.ExternInternOrder;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class LabelSpotsSystematically
{

	public static void setLabelsBasedOnExternIntern( ModelGraph graph, Collection<Spot> center, Collection<Spot> selected, boolean renameUnnamed, boolean renameLabelsEndingWith1Or2 )
	{
		Predicate<Spot> filter = selected == graph.vertices()
				? spot -> true // NB: because graph.vertices().contains(...) is not implemented.
				: selected::contains;
		if(renameUnnamed || renameLabelsEndingWith1Or2)
		{
			BranchFilter branchFilter = new BranchFilter( graph );
			branchFilter.setMatchUnnamed( renameUnnamed );
			if ( renameLabelsEndingWith1Or2 )
				branchFilter.setLabelEndsWith1or2Filter();
			filter = filter.and( branchFilter );
		}
		setLabels( graph, filter, new ExternInternOrder( graph, center ) );
	}

	static void setLabels( ModelGraph graph, Predicate<Spot> filter, Predicate<Spot> correctOrder )
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
