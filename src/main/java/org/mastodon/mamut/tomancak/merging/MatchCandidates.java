package org.mastodon.mamut.tomancak.merging;

import java.util.Comparator;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatialIndex;

public class MatchCandidates
{
	private final double absoluteDistSquCutoff;
	private final double mahalanobisDistSquCutoff;
	private final double ratioThresholdSqu;

	public MatchCandidates( final double distCutoff, final double mahalanobisDistCutoff, final double ratioThreshold )
	{
		absoluteDistSquCutoff = distCutoff * distCutoff;
		mahalanobisDistSquCutoff = mahalanobisDistCutoff * mahalanobisDistCutoff;
		ratioThresholdSqu = ratioThreshold * ratioThreshold;
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		return buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB, final int minTimepoint, final int maxTimepoint )
	{
		return buildMatchingGraph( dsA.model(), dsB.model(), minTimepoint, maxTimepoint );
	}

	public MatchingGraph buildMatchingGraph(final Model mA, final Model mB, final int minTimepoint, final int maxTimepoint )
	{
		final MatchingGraph matching = MatchingGraph.newWithAllSpots( mA, mB );
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > indexA = mA.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			final SpatialIndex< Spot > indexB = mB.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			addCandidates( matching, indexA, indexB );
			addCandidates( matching, indexB, indexA );
		}

		return matching;
	}

	private void addCandidates( final MatchingGraph matching, final SpatialIndex< Spot > indexA, final SpatialIndex< Spot > indexB )
	{
		final SpotMath spotMath = new SpotMath();
		final IncrementalNearestNeighborSearch< Spot > inns = indexB.getIncrementalNearestNeighborSearch();
		for ( final Spot spot1 : indexA )
		{
			inns.search( spot1 );
			while ( inns.hasNext() )
			{
				inns.fwd();
				final double dSqu = inns.getSquareDistance();
				if ( dSqu > absoluteDistSquCutoff )
					break;
				final Spot spot2 = inns.get();
				final double mdSqu = spotMath.mahalanobisDistSqu( spot1, spot2 );
				if ( mdSqu > mahalanobisDistSquCutoff )
					break;
				matching.addEdge(
						matching.getVertex( spot1 ),
						matching.getVertex( spot2 )
				).init( dSqu, mdSqu );
			}
		}
	}

	public MatchingGraph pruneMatchingGraph( final MatchingGraph graph )
	{
		final MatchingGraph matching = MatchingGraph.newWithAllSpots( graph );
		/*
		prune matching graph
			distance must be < distCutoff -- already done
			mahalanobis distance must be < mahalanobisDistCutoff -- already done
			remaining edges qualify for creating conflict if no accepted match is found.
				accepted matches must be mutually nearest neighbors wrt mahalanobis distance
				ratio of mahalanobis distance to 2nd neighbor must be > ratioThreshold



		for each MatchingVertex v1:
			does it have any outgoing edges?
			no -->
				continue

			sort outgoing edges by mahalanobis distance

			do
				add edge[i]
			while |(i+1) <= |edge|) and (mdist(edge[i+1]) / mdist(edge[i]) > th3)
		 */
		final RefList< MatchingEdge > edges = RefCollections.createRefList( graph.edges() );
		for ( final MatchingVertex v : graph.vertices() )
		{
			edges.clear();
			v.outgoingEdges().forEach( edges::add );
			if ( edges.isEmpty() )
				continue;
			edges.sort( Comparator.comparingDouble( MatchingEdge::getMahalDistSqu ) );
			for ( int i = 0; i < edges.size(); ++i )
			{
				final MatchingEdge ge = edges.get( i );
				final MatchingVertex source = matching.getVertex( ge.getSource().getSpot() );
				final MatchingVertex target = matching.getVertex( ge.getTarget().getSpot() );
				matching.addEdge( source, target ).init( ge.getDistSqu(), ge.getMahalDistSqu() );

				if ( i + 1 < edges.size() )
				{
					final MatchingEdge ne = edges.get( i + 1 );
					if ( ne.getMahalDistSqu() / ge.getMahalDistSqu() > ratioThresholdSqu )
						break;
				}
			}
		}

		// corresponding matching.v to graph.v
		final MatchingVertex v = graph.vertices().iterator().next(); // from graph
		matching.getVertex( v.getSpot() );

		return matching;
	}
}
