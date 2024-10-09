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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

/**
 * Utility class that implements most of the functionality
 * provided by the {@link SpatialTrackMatchingPlugin}.
 */
public class SpatialTrackMatchingUtils
{

	/**
	 * Sorts the descendants in the second graph {@link RegisteredGraphs#graphB}
	 * to match the order of the descendants in the first {@link RegisteredGraphs#graphA}.
	 */
	public static void sortSecondTrackSchemeToMatch( RegisteredGraphs result )
	{
		RefList< Spot > spotsToFlipB = getSpotsToFlipB( result );
		FlipDescendants.flipDescendants( result.modelB, spotsToFlipB );
	}

	/**
	 * Copy a tag set from {@link RegisteredGraphs#modelA} to
	 * {@link RegisteredGraphs#modelB}. The tags are copied from the branches in
	 * modelA to the corresponding branches in modelB. The copying is down with
	 * respect to {@link RegisteredGraphs#mapAB}.
	 */
	public static TagSetStructure.TagSet copyTagSetToSecondModel( RegisteredGraphs result,
			TagSetStructure.TagSet tagSetModelA, String newTagSetName )
	{
		List< Pair< String, Integer > > tags = tagSetModelA.getTags().stream()
				.map( t -> Pair.of( t.label(), t.color() ) )
				.collect( Collectors.toList() );
		TagSetStructure.TagSet tagSetModelB = TagSetUtils.addNewTagSetToModel( result.modelB, newTagSetName, tags );
		Function< TagSetStructure.Tag, TagSetStructure.Tag > tagsAB = getTagMap( tagSetModelA, tagSetModelB );
		copyBranchSpotTags( tagSetModelA, result, tagSetModelB, tagsAB );
		copyBranchLinkTags( tagSetModelA, result, tagSetModelB, tagsAB );
		return tagSetModelB;
	}

	private static void copyBranchLinkTags( TagSetStructure.TagSet tagSetModelA, RegisteredGraphs result,
			TagSetStructure.TagSet tagSetModelB, Function< TagSetStructure.Tag, TagSetStructure.Tag > tagsAB )
	{
		ModelGraph graphA = result.graphA;
		ModelGraph graphB = result.graphB;
		Spot refA = graphA.vertexRef();
		Spot refA2 = graphA.vertexRef();
		Spot refB = graphB.vertexRef();
		Spot refB2 = graphB.vertexRef();
		Link erefB = graphB.edgeRef();
		try
		{
			ObjTagMap< Link, TagSetStructure.Tag > edgeTagsA = result.modelA.getTagSetModel().getEdgeTags().tags( tagSetModelA );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTagsB = result.modelB.getTagSetModel().getEdgeTags().tags( tagSetModelB );
			for ( Spot spotA : result.mapAB.keySet() )
			{
				Spot spotB = result.mapAB.get( spotA, refB );
				Spot branchEndA = BranchGraphUtils.getBranchEnd( spotA, refA );
				Spot branchEndB = BranchGraphUtils.getBranchEnd( spotB, refB );
				for ( Link linkA : branchEndA.outgoingEdges() )
				{
					Spot targetA = linkA.getTarget( refA2 );
					Spot targetB = result.mapAB.get( targetA, refB2 );
					Link linkB = graphB.getEdge( branchEndB, targetB, erefB );
					if ( linkB == null )
						continue;
					edgeTagsB.set( linkB, tagsAB.apply( edgeTagsA.get( linkA ) ) );
				}
			}
		}
		finally
		{
			graphA.releaseRef( refA );
			graphA.releaseRef( refA2 );
			graphB.releaseRef( refB );
			graphB.releaseRef( refB2 );
			graphB.releaseRef( erefB );
		}
	}

	private static void copyBranchSpotTags( TagSetStructure.TagSet tagSetModelA, RegisteredGraphs result,
			TagSetStructure.TagSet tagSetModelB,
			Function< TagSetStructure.Tag, TagSetStructure.Tag > tagsAB )
	{
		for ( Spot spotA : result.mapAB.keySet() )
		{
			Spot spotB = result.mapAB.get( spotA );
			TagSetStructure.Tag tagA = TagSetUtils.getBranchTag( result.modelA, tagSetModelA, spotA );
			TagSetStructure.Tag tagB = tagsAB.apply( tagA );
			TagSetUtils.tagBranch( result.modelB, tagSetModelB, tagB, spotB );
		}
	}

	private static Function< TagSetStructure.Tag, TagSetStructure.Tag > getTagMap( TagSetStructure.TagSet tagSetModelA,
			TagSetStructure.TagSet tagSetModelB )
	{
		List< TagSetStructure.Tag > tagsA = tagSetModelA.getTags();
		List< TagSetStructure.Tag > tagsB = tagSetModelB.getTags();
		Map< TagSetStructure.Tag, TagSetStructure.Tag > map = new HashMap<>();
		if ( tagsA.size() != tagsB.size() )
			throw new IllegalArgumentException( "TagSets must have the same number of tags." );
		for ( int i = 0; i < tagsA.size(); i++ )
		{
			TagSetStructure.Tag tagA = tagsA.get( i );
			TagSetStructure.Tag tagB = tagsB.get( i );
			if ( !Objects.equals( tagA.label(), tagB.label() ) )
				throw new IllegalArgumentException( "TagSets must have the same tags." );
			map.put( tagA, tagB );
		}
		return map::get;
	}

	/**
	 * Creates a new tag set in both models. The tag set has two tags:
	 * "not mapped" and "flipped". The cells / branches in
	 * {@link RegisteredGraphs#modelA} and {@link RegisteredGraphs#modelB}
	 * are tagged according to the mapping {@link RegisteredGraphs#mapAB}.
	 *
	 * and tags unmatched and flipped cells / branches.
	 */
	public static void tagCells( RegisteredGraphs result, boolean modifyA, boolean modifyB )
	{
		if ( modifyA )
			tagSpotsA( result );
		if ( modifyB )
			tagSpotsA( result.swapAB() );
	}

	private static void tagSpotsA( RegisteredGraphs result )
	{
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( result.modelA, "spatial track matching", Arrays.asList(
				Pair.of( "not mapped", 0xff00ccff ),
				Pair.of( "flipped", 0xffeeaa00 )
		) );
		TagHelper notMapped = new TagHelper( result.modelA, tagSet, tagSet.getTags().get( 0 ) );
		TagHelper flipped = new TagHelper( result.modelA, tagSet, tagSet.getTags().get( 1 ) );
		tagUnmatchedSpotsInModelA( result, notMapped );
		tagFlippedSpotsInModelA( result, flipped );
	}

	private static void tagFlippedSpotsInModelA( RegisteredGraphs result, TagHelper tag )
	{
		Model modelA = result.modelA;
		Spot ref = modelA.getGraph().vertexRef();
		try
		{
			for ( Spot spot : getSpotsToFlipA( result ) )
				for ( Link link : spot.outgoingEdges() )
				{
					tag.tagLink( link );
					tag.tagBranch( link.getTarget( ref ) );
				}
		}
		finally
		{
			modelA.getGraph().releaseRef( ref );
		}
	}

	private static void tagUnmatchedSpotsInModelA( RegisteredGraphs result, TagHelper tag )
	{
		for ( Spot spot : getUnmatchSpotsA( result ) )
		{
			tag.tagBranch( spot );
			for ( Link link : spot.incomingEdges() )
				tag.tagLink( link );
		}
	}

	private static RefList< Spot > getSpotsToFlipB( RegisteredGraphs result )
	{
		return getSpotsToFlipA( result.swapAB() );
	}

	/**
	 * Returns the spots in {@link RegisteredGraphs#graphA} that need to be
	 * flipped in order for the descendants of graph A to match the order
	 * of the descendants in graph B.
	 */
	public static RefList< Spot > getSpotsToFlipA( RegisteredGraphs r )
	{
		Spot refA = r.graphA.vertexRef();
		Spot refB = r.graphB.vertexRef();
		Spot refB0 = r.graphB.vertexRef();
		try
		{
			RefArrayList< Spot > list = new RefArrayList<>( r.graphA.vertices().getRefPool() );
			for ( Spot spotA : r.mapAB.keySet() )
			{
				Spot spotB = r.mapAB.get( spotA, refB0 );
				Spot dividingA = BranchGraphUtils.getBranchEnd( spotA, refA );
				Spot dividingB = BranchGraphUtils.getBranchEnd( spotB, refB );
				if ( doesRequireFlip( r, dividingA, dividingB ) )
					list.add( dividingA );
			}
			return list;
		}
		finally
		{
			r.graphA.releaseRef( refA );
			r.graphB.releaseRef( refB );
			r.graphB.releaseRef( refB0 );
		}
	}

	/**
	 * Returns true if the descendants of dividingA are in the opposite order
	 * to the descendants of dividingB. Which descendant corresponds to which
	 * is determined by the mapping {@link RegisteredGraphs#mapAB}.
	 */
	private static boolean doesRequireFlip( RegisteredGraphs r, Spot dividingA, Spot dividingB )
	{
		Spot refA = r.graphA.vertexRef();
		Spot refB = r.graphB.vertexRef();
		Spot refB2 = r.graphB.vertexRef();
		try
		{
			boolean bothDivide = dividingA.outgoingEdges().size() == 2 &&
					dividingB.outgoingEdges().size() == 2;
			if ( !bothDivide )
				return false;
			Spot firstChildA = dividingA.outgoingEdges().get( 0 ).getTarget( refA );
			Spot secondChildB = dividingB.outgoingEdges().get( 1 ).getTarget( refB );
			return r.mapAB.get( firstChildA, refB2 ).equals( secondChildB );
		}
		finally
		{
			r.graphA.releaseRef( refA );
			r.graphB.releaseRef( refB );
			r.graphB.releaseRef( refB2 );
		}
	}

	/**
	 * Returns a list of branch starts in graph A that are not mapped to any
	 * branch in graph B.
	 */
	public static RefSet< Spot > getUnmatchSpotsA( RegisteredGraphs r )
	{
		RefSet< Spot > branchStarts = BranchGraphUtils.getAllBranchStarts( r.graphA );
		branchStarts.removeAll( r.mapAB.keySet() );
		return branchStarts;
	}

	/**
	 * Shows a plot: cell division angles vs. cell division timepoint.
	 */
	public static void plotAngleAgainstTimepoint( RefDoubleMap< Spot > angles )
	{
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYSeries series = new XYSeries( "Angles" );
		Spot ref = angles.keySet().iterator().next();
		for ( Spot spot : angles.keySet() )
		{
			final double angle = angles.get( spot );
			series.add( BranchGraphUtils.getBranchEnd( spot, ref ).getTimepoint(), angle );
		}
		dataset.addSeries( series );
		String title = "Angles between paired cell division directions";
		final JFreeChart chart = ChartFactory.createScatterPlot( title, "timepoint", "angle", dataset );
		chart.getXYPlot().getRangeAxis().setRange( 0, 180 );

		final ChartFrame frame = new ChartFrame( title, chart );
		frame.pack();
		frame.setVisible( true );
	}

	public static void copySpotLabelsFromAtoB( final RegisteredGraphs registration )
	{
		final ModelGraph graphB = registration.graphB;
		final Spot refB = graphB.vertexRef();
		try
		{
			for ( final Spot spotA : registration.mapAB.keySet() )
			{
				boolean hasLabel = !Integer.toString( spotA.getInternalPoolIndex() ).equals( spotA.getLabel() );
				if ( hasLabel )
				{
					final Spot spotB = registration.mapAB.get( spotA, refB );
					final RefList< Spot > spotsOfBranchB = BranchGraphUtils.getBranchSpotsAndLinks( graphB, spotB ).getA();
					for ( final Spot spot : spotsOfBranchB )
						spot.setLabel( spotA.getLabel() );
				}
			}
		}
		finally
		{
			graphB.releaseRef( refB );
		}
	}
}
