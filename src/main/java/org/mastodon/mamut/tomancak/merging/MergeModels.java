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
package org.mastodon.mamut.tomancak.merging;

import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.graph.algorithm.traversal.UndirectedDepthFirstIterator;
import org.mastodon.mamut.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.spots.InterpolateMissingSpots;
import org.mastodon.mamut.tomancak.merging.MergeDatasets.OutputDataSet;
import org.mastodon.mamut.tomancak.merging.MergeTags.TagSetStructureMaps;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.spatial.SpatialIndex;

public class MergeModels
{
	static int getMaxNonEmptyTimepoint( final Model m )
	{
		int maxTimepoint = 0;
		for ( final Spot s : m.getGraph().vertices() )
			maxTimepoint = Math.max( maxTimepoint, s.getTimepoint() );
		return maxTimepoint;
	}

	public static void merge( final Model mA, final Model mB, final OutputDataSet output,
			final double distCutoff, final double mahalanobisDistCutoff, final double ratioThreshold )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( getMaxNonEmptyTimepoint( mA ), getMaxNonEmptyTimepoint( mB ) );
		merge( mA, mB, output, minTimepoint, maxTimepoint, distCutoff, mahalanobisDistCutoff, ratioThreshold );
	}

	public static void merge( final Model mA, final Model mB, final OutputDataSet output,
			final int minTimepoint, final int maxTimepoint,
			final double distCutoff, final double mahalanobisDistCutoff, final double ratioThreshold )
	{
		new ModelImporter( output.getModel() ){{ startImport(); }};

		InterpolateMissingSpots.interpolate( mA );
		InterpolateMissingSpots.interpolate( mB );

		final MatchCandidates candidates = new MatchCandidates( distCutoff, mahalanobisDistCutoff, ratioThreshold );
		final MatchingGraph matching = candidates.pruneMatchingGraph( candidates.buildMatchingGraph( mA, mB, minTimepoint, maxTimepoint ) );
		final MatchingGraphUtils utils = new MatchingGraphUtils( matching );

		final Tag tagA = output.addSourceTag( "A", 0xffffff00 );
		final Tag tagB = output.addSourceTag( "B", 0xffff00ff );
		final Tag tagSingletonA = output.addConflictTag( "Singleton A", 0xffffffcc );
		final Tag tagSingletonB = output.addConflictTag( "Singleton B", 0xffffccff );
		final Tag tagMatchAB = output.addConflictTag( "MatchAB", 0xffccffcc );
		final Tag tagConflict = output.addConflictTag( "Conflict", 0xffff0000 );
		final Tag tagTagConflict = output.addTagConflictTag( "Tag Conflict", 0xffff0000 );
		final Tag tagLabelConflict = output.addLabelConflictTag( "Label Conflict", 0xffff0000 );

		final ModelGraph graph = output.getModel().getGraph();
		final ObjTags< Spot > vertexTags = output.getModel().getTagSetModel().getVertexTags();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final Spot vref = graph.vertexRef();

/*
		for every spot a in A:
			add a' with shape and translated a tags
			add mapping MA: a --> a'
*/
		final ModelGraph graphA = mA.getGraph();
		final RefRefMap< Spot, Spot > mapAtoDest = RefMaps.createRefRefMap( graphA.vertices(), graph.vertices() );
		for ( final Spot spotA : graphA.vertices() )
		{
			final int tp = spotA.getTimepoint();
			spotA.localize( pos );
			spotA.getCovariance( cov );
			final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
			vertexTags.set( destSpot, tagA );
			vertexTags.set( destSpot, tagSingletonA );
			mapAtoDest.put( spotA, destSpot );
		}

/*
		for every edge (a1,a2) in A
			get a1', a2' from mapping MA
			add edge (a1',a2') and translated (a1,a2) tags
*/
		final RefRefMap< Link, Link > mapAtoDestLinks = RefMaps.createRefRefMap( graphA.edges(), graph.edges() );
		for ( final Link linkA : graphA.edges() )
		{
			final Spot source = mapAtoDest.get( linkA.getSource() );
			final Spot target = mapAtoDest.get( linkA.getTarget() );
			final Link destLink = graph.addEdge( source, target );
			mapAtoDestLinks.put( linkA, destLink );
		}

/*
		for every spot b in B, ordered by ascending timepoint!:
			if singleton (b):
				add b' with shape and translated b tags
				add mapping MB: b --> b'
			else if perfect match (a,b):
				get a'
				if b has incoming edge (c,b) AND a has incoming edge (d,a)
					get c', d'
					if c' != d':
						add b' with shape and translated b tags
						add mapping MB: b --> b'
						add "conflict" tag to a' and b'
						continue;
				(else:)
					add translated b tags, checking for conflicts
					add mapping MB: b --> a'
			else
				add b' with shape and translated b tags
				add "conflict" tag to b' and any connected (and already present) c'
				add mapping MB: b --> b'
*/
		final ModelGraph graphB = mB.getGraph();
		final RefRefMap< Spot, Spot > mapBtoDest = RefMaps.createRefRefMap( graphA.vertices(), graph.vertices() );
		final UndirectedDepthFirstIterator< MatchingVertex, MatchingEdge > miter = new UndirectedDepthFirstIterator<>( matching );
		for ( int timepoint = 0; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > indexB = mB.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( final Spot spotB : indexB )
			{
				final MatchingVertex mvB = matching.getVertex( spotB );

				if ( utils.isUnmatched( mvB ) )
				{
					final int tp = spotB.getTimepoint();
					spotB.localize( pos );
					spotB.getCovariance( cov );
					final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
					vertexTags.set( destSpot, tagB );
					vertexTags.set( destSpot, tagSingletonB );
					mapBtoDest.put( spotB, destSpot );
				}
				else if ( utils.isPerfectlyMatched( mvB ) )
				{
					final MatchingVertex mvA = mvB.outgoingEdges().get( 0 ).getTarget();
					final Spot spotA = mvA.getSpot( vref );
					final Spot destSpotA = mapAtoDest.get( spotA );
					if ( ! ( spotB.incomingEdges().isEmpty() || spotA.incomingEdges().isEmpty() ) )
					{
						final Spot spotC = spotB.incomingEdges().get( 0 ).getSource();
						final Spot spotD = spotA.incomingEdges().get( 0 ).getSource();
						final Spot destSpotC = mapBtoDest.get( spotC );
						final Spot destSpotD = mapAtoDest.get( spotD );
						if ( !destSpotC.equals( destSpotD ) )
						{
							final int tp = spotB.getTimepoint();
							spotB.localize( pos );
							spotB.getCovariance( cov );
							final Spot destSpotB = graph.addVertex( vref ).init( tp, pos, cov );
							vertexTags.set( destSpotB, tagB );
							mapBtoDest.put( spotB, destSpotB );
							vertexTags.set( destSpotB, tagConflict );
							vertexTags.set( destSpotA, tagConflict );
							continue;
						}
					}
					vertexTags.set( destSpotA, tagB );
					vertexTags.set( destSpotA, tagMatchAB );
					mapBtoDest.put( spotB, destSpotA );
				}
				else
				{
					final int tp = spotB.getTimepoint();
					spotB.localize( pos );
					spotB.getCovariance( cov );
					final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
					vertexTags.set( destSpot, tagB );
					mapBtoDest.put( spotB, destSpot );

					miter.reset( mvB );
					while ( miter.hasNext() )
					{
						final MatchingVertex mv = miter.next();
						final Spot sourceSpot = mv.getSpot();
						final Spot spot;
						if ( sourceSpot.getModelGraph() == graphA )
							spot = mapAtoDest.get( sourceSpot );
						else
							spot = mapBtoDest.get( sourceSpot );
						if ( spot != null )
							vertexTags.set( spot, tagConflict );
					}
				}
			}
		}

/*
		for every edge (b1,b2) in B
			get b1', b2' from mapping MB
			add edge (b1',b2') if not exists
			add translated (b1,b2) tags, checking for conflicts
*/
		final RefRefMap< Link, Link > mapBtoDestLinks = RefMaps.createRefRefMap( graphB.edges(), graph.edges() );
		for ( final Link linkB : graphB.edges() )
		{
			final Spot source = mapBtoDest.get( linkB.getSource() );
			final Spot target = mapBtoDest.get( linkB.getTarget() );
			Link destLink = graph.getEdge( source, target );
			if ( destLink == null )
				destLink = graph.addEdge( source, target );
			mapBtoDestLinks.put( linkB, destLink );
		}




		/*
		 * ========================================
		 *           transfer tags
		 * ========================================
		 */

		final TagSetModel< Spot, Link > tsm = output.getModel().getTagSetModel();
		final TagSetStructure tss = output.getTagSetStructure();
		final TagSetModel< Spot, Link > tsmA = mA.getTagSetModel();
		final TagSetModel< Spot, Link > tsmB = mB.getTagSetModel();
		final TagSetStructure tssA = tsmA.getTagSetStructure();
		final TagSetStructure tssB = tsmB.getTagSetStructure();
		final TagSetStructureMaps tssAtoCopy = MergeTags.addTagSetStructureCopy( tss, tssA, "((A)) " );
		final TagSetStructureMaps tssBtoCopy = MergeTags.addTagSetStructureCopy( tss, tssB, "((B)) " );
		final TagSetStructureMaps tssAtoDest = MergeTags.mergeTagSetStructure( tss, tssA );
		final TagSetStructureMaps tssBtoDest = MergeTags.mergeTagSetStructure( tss, tssB );
		output.updateTagSetModel();

/*
		for every spot a in A:
			get a'
			for every tagset in A:
				get tag t of (a, tagset)
				if t exists:
					get t' as copy ((A)) of t
					set t' for a'
					get t" as merge of t
					set t" for a'
		analogous for links in A...
*/
		for ( final Spot spotA : graphA.vertices() )
		{
			final Spot destSpot = mapAtoDest.get( spotA );
			for ( final TagSet tagSet : tssA.getTagSets() )
			{
				final Tag tag = tsmA.getVertexTags().tags( tagSet ).get( spotA );
				if ( tag != null )
				{
					// copy ((A))
					tsm.getVertexTags().set( destSpot, tssAtoCopy.tagMap.get( tag ) );

					// merged
					tsm.getVertexTags().set( destSpot, tssAtoDest.tagMap.get( tag ) );
				}
			}
		}
		for ( final Link linkA : graphA.edges() )
		{
			final Link destLink = mapAtoDestLinks.get( linkA );
			for ( final TagSet tagSet : tssA.getTagSets() )
			{
				final Tag tag = tsmA.getEdgeTags().tags( tagSet ).get( linkA );
				if ( tag != null )
				{
					// copy ((A))
					tsm.getEdgeTags().set( destLink, tssAtoCopy.tagMap.get( tag ) );

					// merged
					tsm.getEdgeTags().set( destLink, tssAtoDest.tagMap.get( tag ) );
				}
			}
		}

/*
		for every spot b in B:
			get b'
			for every tagset in B:
				get tag t of (b, tagset)
				if t != null:
					get t' as copy ((B)) of t
					set t' for b'
					get t" as merge of t
					get tagset" as merge of tagset
					get tag x" og (b', tagset")
					if x" exists and x" != t":
						mark tag conflict for b'
					else:
						set t" for b'
		analogous for links in B...
*/
		for ( final Spot spotB : graphB.vertices() )
		{
			final Spot destSpot = mapBtoDest.get( spotB );
			for ( final TagSet tagSet : tssB.getTagSets() )
			{
				final Tag tag = tsmB.getVertexTags().tags( tagSet ).get( spotB );
				if ( tag != null )
				{
					// copy ((B))
					tsm.getVertexTags().set( destSpot, tssBtoCopy.tagMap.get( tag ) );

					// merged
					final TagSet destTagSet = tssBtoDest.tagSetMap.get( tagSet );
					final Tag destTag = tsm.getVertexTags().tags( destTagSet ).get( destSpot );
					final Tag expectedDestTag = tssBtoDest.tagMap.get( tag );
					if ( destTag == null )
						tsm.getVertexTags().set( destSpot, expectedDestTag );
					else if ( !destTag.equals( expectedDestTag ) )
						tsm.getVertexTags().set( destSpot, tagTagConflict );
				}
			}
		}
		for ( final Link linkB : graphB.edges() )
		{
			final Link destLink = mapBtoDestLinks.get( linkB );
			for ( final TagSet tagSet : tssB.getTagSets() )
			{
				final Tag tag = tsmB.getEdgeTags().tags( tagSet ).get( linkB );
				if ( tag != null )
				{
					// copy ((B))
					tsm.getEdgeTags().set( destLink, tssBtoCopy.tagMap.get( tag ) );

					// merged
					final TagSet destTagSet = tssBtoDest.tagSetMap.get( tagSet );
					final Tag destTag = tsm.getEdgeTags().tags( destTagSet ).get( destLink );
					final Tag expectedDestTag = tssBtoDest.tagMap.get( tag );
					if ( destTag == null )
						tsm.getEdgeTags().set( destLink, expectedDestTag );
					else if ( !destTag.equals( expectedDestTag ) )
						tsm.getEdgeTags().set( destLink, tagTagConflict );
				}
			}
		}




		/*
		 * ========================================
		 *           transfer labels
		 * ========================================
		 */

		for ( final Spot spotA : graphA.vertices() )
		{
			if ( MergingUtil.hasLabel( spotA ) )
			{
				final Spot destSpot = mapAtoDest.get( spotA );
				destSpot.setLabel( spotA.getLabel() );
			}
		}

		for ( final Spot spotB : graphB.vertices() )
		{
			if ( MergingUtil.hasLabel( spotB ) )
			{
				final Spot destSpot = mapBtoDest.get( spotB );
				final String lB = spotB.getLabel();
				if ( MergingUtil.hasLabel( destSpot ) )
				{
					final String lA = destSpot.getLabel();
					if ( !lA.equals( lB ) )
					{
						destSpot.setLabel( destSpot.getLabel() + " @@@ " + spotB.getLabel() );
						tsm.getVertexTags().set( destSpot, tagLabelConflict );
					}
				}
				else
				{
					destSpot.setLabel( lB );
				}
			}
		}

		new ModelImporter( output.getModel() ){{ finishImport(); }};
	}

	private static class MatchingGraphUtils
	{
		private final MatchingEdge eref1;
		private final MatchingEdge eref2;
		private final MatchingVertex vref1;
		private final MatchingVertex vref2;

		public MatchingGraphUtils( final MatchingGraph matchingGraph )
		{
			eref1 = matchingGraph.edgeRef();
			eref2 = matchingGraph.edgeRef();
			vref1 = matchingGraph.vertexRef();
			vref2 = matchingGraph.vertexRef();
		}

		public boolean isUnmatched( final MatchingVertex mv )
		{
			return mv.edges().isEmpty();
		}

		/**
		 * {@code true} if the best target of {@code mv}, has {@code mv} as its
		 * best target in return. Assumes that outgoing edges are sorted by
		 * increasing mahalanobis distance.
		 * 
		 * @param mv
		 *            the vertex.
		 * @return <code>true</code> if the best target of {@code mv}, has
		 *         {@code mv} as its best target in return.
		 */
		public boolean isPerfectlyMatched( final MatchingVertex mv )
		{
			if ( mv.outgoingEdges().isEmpty() )
				return false;
			final MatchingVertex target = mv.outgoingEdges().get( 0, eref1 ).getTarget( vref1 );
			if ( target.outgoingEdges().isEmpty() )
				return false;
			final MatchingVertex targetsTarget = target.outgoingEdges().get( 0, eref2 ).getTarget( vref2 );
			return targetsTarget.equals( mv );
		}
	}
}
