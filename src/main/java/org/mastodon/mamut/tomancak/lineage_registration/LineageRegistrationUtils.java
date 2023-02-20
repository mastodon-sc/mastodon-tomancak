package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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

	public static TagSetStructure.TagSet copyTagSetToSecond( Model modelA, Model modelB, TagSetStructure.TagSet tagSetModelA )
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		TagSetStructure.TagSet tagSetModelB = LineageColoring.copyTagSetToModel( tagSetModelA, modelB );
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
				Spot branchEndA = LineageTreeUtils.getBranchEnd( spotA, refA );
				Spot branchEndB = LineageTreeUtils.getBranchEnd( spotB, refB );
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
			TagSetStructure.Tag tagA = LineageRegistrationUtils.getBranchTag( modelA, tagSetModelA, spotA );
			TagSetStructure.Tag tagB = tagsAB.apply( tagA );
			LineageRegistrationUtils.tagBranch( modelB, spotB, tagSetModelB, tagB );
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
		// TODO write unit test
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		if ( modifyA )
			tagSpotsA( modelA, result );
		if ( modifyB )
			tagSpotsA( modelB, result.swapAB() );
	}

	private static void tagSpotsA( Model modelA, RegisteredGraphs result )
	{
		Map< String, Integer > tags = new HashMap<>();
		String notMapped = "not mapped";
		tags.put( notMapped, 0xffff2222 );
		String flipped = "flipped";
		tags.put( flipped, 0xff22ff22 );
		TagSetStructure.TagSet tagSet = LineageColoring.addTagSetToModel( modelA, "registration", tags );
		tagBranches( modelA, tagSet, notMapped, getUnmatchSpotsA( result ) );
		tagBranches( modelA, tagSet, flipped, getSpotsToFlipA( result ) );
	}

	public static void tagBranches( Model model, TagSetStructure.TagSet tagSet, String name, Collection< Spot > branchStrats )
	{
		TagSetStructure.Tag tag = LineageColoring.findTag( tagSet, name );
		for ( Spot spot : branchStrats )
			tagBranch( model, spot, tagSet, tag );
	}

	public static void tagBranch( Model model, Spot spot, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		// TODO move to other class
		ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			vertexTags.set( spot, tag );
			//forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				edgeTags.set( link, tag );
				vertexTags.set( s, tag );
			}
			// backward
			s.refTo( spot );
			while ( s.incomingEdges().size() == 1 )
			{
				Link link = s.incomingEdges().get( 0 );
				s = link.getSource( s );
				if ( s.outgoingEdges().size() != 1 )
					break;
				edgeTags.set( link, tag );
				vertexTags.set( s, tag );
			}
		}
		finally
		{
			graphA.releaseRef( s );
		}
	}

	public static TagSetStructure.Tag getBranchTag( Model model, TagSetStructure.TagSet tagSet, Spot spot )
	{
		// TODO move to other class
		ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			ObjTagMap< Spot, TagSetStructure.Tag > vertexTags = model.getTagSetModel().getVertexTags().tags( tagSet );
			ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
			TagSetStructure.Tag tag = vertexTags.get( spot );
			if ( tag == null )
				return null;
			//forward
			s.refTo( spot );
			while ( s.outgoingEdges().size() == 1 )
			{
				Link link = s.outgoingEdges().get( 0 );
				s = link.getTarget( s );
				if ( s.incomingEdges().size() != 1 )
					break;
				if ( !tag.equals( edgeTags.get( link ) ) )
					return null;
				if ( !tag.equals( vertexTags.get( s ) ) )
					return null;
			}
			return tag;
		}
		finally
		{
			graphA.releaseRef( s );
		}
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
				Spot dividingA = LineageTreeUtils.getBranchEnd( spotA, refA );
				Spot dividingB = LineageTreeUtils.getBranchEnd( spotB, refB );
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
