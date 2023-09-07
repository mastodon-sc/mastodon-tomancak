package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

public class GraphUtils
{
	/**
	 * @return the set of root nodes of the given graph.
	 */
	public static < V extends Vertex< ? > > RefSet< V > getRoots( ReadOnlyGraph< V, ? > graph )
	{
		Predicate< V > isRoot = spot -> spot.incomingEdges().isEmpty();
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	/**
	 * Returns the vertices of the graph that would be roots if all vertices with
	 * {@code vertex.getTimepoint() < timepoint} would be discarded.
	 */
	public static < V extends Vertex< E > & HasTimepoint, E extends Edge< V > > RefSet< V > getRootsAfterTimepoint( ReadOnlyGraph< V, E > graph, int timepoint )
	{
		V ref = graph.vertexRef();
		try
		{
			Predicate< V > isRoot = spot -> {

				if ( spot.getTimepoint() < timepoint )
					return false;

				if ( spot.incomingEdges().isEmpty() )
					return true;

				for ( E edge : spot.incomingEdges() )
				{
					V source = edge.getSource( ref );
					if ( source.getTimepoint() >= timepoint )
						return false;
				}

				return true;
			};
			return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}

	/**
	 * Returns the set of leaf nodes of the given graph.
	 */
	public static < V extends Vertex< ? > > RefSet< V > getLeafs( ReadOnlyGraph< V, ? > graph )
	{
		Predicate< V > isLeaf = spot -> spot.outgoingEdges().isEmpty();
		return RefCollectionUtils.filterSet( graph.vertices(), isLeaf );
	}
}
