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
package org.mastodon.mamut.tomancak.trackmatching;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

/**
 * Utility methods for working with a {@link org.mastodon.graph.branch.BranchGraph branch graphs}.
 *
 * @author Matthias Arzt
 */
public class BranchGraphUtils
{
	private BranchGraphUtils()
	{
		// prevent instantiation
	}

	/**
	 * Returns the first vertex of the branch, that also contains the given vertex.
	 *
	 * @param spot This that vertex defines the branch. It can be any vertex of the branch.
	 * @param ref  Buffer that might be used to store the result.
	 * @param <V>  The vertex type.
	 * @param <E>  The edge type.
	 * @return The first vertex of the branch.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > V getBranchStart( V spot, V ref )
	{
		V s = spot;
		while ( s.incomingEdges().size() == 1 )
		{
			E edge = s.incomingEdges().iterator().next();
			s = edge.getSource( ref );
			if ( s.outgoingEdges().size() != 1 )
				return edge.getTarget( ref );
		}
		return s;
	}

	/**
	 * Returns the last vertex of the branch, that also contains the given vertex.
	 *
	 * @param spot This vertex that defines the branch. It can be any vertex of the branch.
	 * @param ref  Buffer that might be used to store the result.
	 * @param <V>  The vertex type.
	 * @param <E>  The edge type.
	 * @return Last vertex of the branch.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > V getBranchEnd( V spot, V ref )
	{
		V s = spot;
		while ( s.outgoingEdges().size() == 1 )
		{
			E edge = s.outgoingEdges().iterator().next();
			s = edge.getTarget( ref );
			if ( s.incomingEdges().size() != 1 )
				return edge.getSource( ref );
		}
		return s;
	}

	/**
	 * Returns a set of all vertices that are the first vertex of a branch.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > RefSet< V > getAllBranchStarts( ReadOnlyGraph< V, E > graph )
	{
		V ref = graph.vertexRef();
		try
		{
			RefSet< V > set = RefCollections.createRefSet( graph.vertices() );
			for ( V spot : graph.vertices() )
			{
				if ( spot.incomingEdges().size() != 1 )
					set.add( spot );
				if ( spot.outgoingEdges().size() > 1 )
					for ( E link : spot.outgoingEdges() )
						set.add( link.getTarget( ref ) );
			}
			return set;
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}

	/**
	 * Returns true if the branch, that contains the given spot, divides.
	 * (A branch divides if it's last vertex has at least two outgoing edges.)
	 *
	 * @param spot The spot that defines the branch. It can be any vertex of the branch.
	 * @param ref  Buffer that might be used during computation.
	 * @param <V>  The vertex type.
	 * @param <E>  The edge type.
	 * @return True if the branch divides.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > boolean doesBranchDivide( final V spot, final V ref )
	{
		V branchEnd = getBranchEnd( spot, ref );
		return branchEnd.outgoingEdges().size() > 1;
	}

	/**
	 * Returns the vertex of the specified branch that is at the given time point.
	 *
	 * @param branchStart The first vertex of the branch.
	 * @param timePoint   The time point.
	 * @param ref         Buffer that might be used to store the result.
	 * @param <V>         The vertex type.
	 * @param <E>         The edge type.
	 * @return Vertex of the branch at the given time point.
	 */
	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > V findVertexForTimePoint( V branchStart, int timePoint, V ref )
	{
		V spot = branchStart;
		if ( spot.getTimepoint() >= timePoint )
			return spot;
		while ( spot.outgoingEdges().size() == 1 )
		{
			spot = spot.outgoingEdges().iterator().next().getTarget( ref );
			if ( spot.getTimepoint() >= timePoint )
				return spot;
		}
		return spot;
	}

	/**
	 * Returns a list of all vertices and a list of all edges of the branch that starts at the given vertex.
	 *
	 * @param graph       The graph the contains the branch.
	 * @param branchStart The first vertex of the branch.
	 * @param <V>         The vertex type.
	 * @param <E>         The edge type.
	 * @return A pair of lists, the first one containing the vertices of the branch, the second one the edges of the branch
	 */
	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
	Pair< RefList< V >, RefList< E > > getBranchSpotsAndLinks( ReadOnlyGraph< V, E > graph, V branchStart )
	{
		RefList< E > links = RefCollections.createRefList( graph.edges() );
		RefList< V > spots = RefCollections.createRefList( graph.vertices() );
		spots.add( branchStart );
		V ref = graph.vertexRef();
		V spot = branchStart;
		while ( spot.outgoingEdges().size() == 1 )
		{
			E link = spot.outgoingEdges().iterator().next();
			spot = link.getTarget( ref );
			if ( spot.incomingEdges().size() != 1 )
				break;
			links.add( link );
			spots.add( spot );
		}
		graph.releaseRef( ref );
		return new ValuePair<>( spots, links );
	}
}
