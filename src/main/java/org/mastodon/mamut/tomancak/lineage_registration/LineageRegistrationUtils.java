package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
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

public class LineageRegistrationUtils
{

	public static void sortSecondTrackSchemeToMatch( Model modelA, Model modelB )
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		FlipDescendants.flipDescendants( modelB, getSpotsToFlipB( result ) );
	}

	public static TagSetStructure.TagSet copyTagSetToSecond( Model modelA, Model modelB,
			TagSetStructure.TagSet tagSetModelA, String newTagSetName )
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		List< Pair< String, Integer > > tags = tagSetModelA.getTags().stream()
				.map( t -> Pair.of( t.label(), t.color() ) )
				.collect( Collectors.toList() );
		TagSetStructure.TagSet tagSetModelB = TagSetUtils.addNewTagSetToModel( modelB, tagSetModelA.getName(), tags );
		Function< TagSetStructure.Tag, TagSetStructure.Tag > tagsAB = getTagMap( tagSetModelA, tagSetModelB );
		copyBranchSpotTags( modelA, modelB, tagSetModelA, result, tagSetModelB, tagsAB );
		copyBranchLinkTags( modelA, modelB, tagSetModelA, result, tagSetModelB, tagsAB );
		return tagSetModelB;
	}

	private static void copyBranchLinkTags( Model modelA, Model modelB, TagSetStructure.TagSet tagSetModelA, RegisteredGraphs result,
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
			ObjTagMap< Link, TagSetStructure.Tag > edgeTagsA = modelA.getTagSetModel().getEdgeTags().tags( tagSetModelA );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTagsB = modelB.getTagSetModel().getEdgeTags().tags( tagSetModelB );
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

	private static void copyBranchSpotTags( Model modelA, Model modelB, TagSetStructure.TagSet tagSetModelA, RegisteredGraphs result,
			TagSetStructure.TagSet tagSetModelB,
			Function< TagSetStructure.Tag, TagSetStructure.Tag > tagsAB )
	{
		for ( Spot spotA : result.mapAB.keySet() )
		{
			Spot spotB = result.mapAB.get( spotA );
			TagSetStructure.Tag tagA = TagSetUtils.getBranchTag( modelA, tagSetModelA, spotA );
			TagSetStructure.Tag tagB = tagsAB.apply( tagA );
			TagSetUtils.tagBranch( modelB, tagSetModelB, tagB, spotB );
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

	public static void tagCells( Model modelA, Model modelB, boolean modifyA, boolean modifyB )
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		if ( modifyA )
			tagSpotsA( modelA, result );
		if ( modifyB )
			tagSpotsA( modelB, result.swapAB() );
	}

	private static void tagSpotsA( Model modelA, RegisteredGraphs result )
	{
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( modelA, "lineage registration", Arrays.asList(
				Pair.of( "not mapped", 0xffff2222 ),
				Pair.of( "flipped", 0xff22ff22 )
		) );
		tagBranches( modelA, tagSet, tagSet.getTags().get( 0 ), getUnmatchSpotsA( result ) );
		tagBranches( modelA, tagSet, tagSet.getTags().get( 1 ), getSpotsToFlipA( result ) );
	}

	private static void tagBranches( Model model, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag, Collection< Spot > spots )
	{
		for ( Spot spot : spots )
			TagSetUtils.tagBranch( model, tagSet, tag, spot );
	}

	private static RefList< Spot > getSpotsToFlipB( RegisteredGraphs result )
	{
		return getSpotsToFlipA( result.swapAB() );
	}

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

	public static RefSet< Spot > getUnmatchSpotsA( RegisteredGraphs r )
	{
		RefSet< Spot > branchStarts = BranchGraphUtils.getAllBranchStarts( r.graphA );
		branchStarts.removeAll( r.mapAB.keySet() );
		return branchStarts;
	}

}
