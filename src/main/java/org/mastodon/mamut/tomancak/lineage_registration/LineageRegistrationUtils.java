package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetStructure;

public class LineageRegistrationUtils
{

	public static void sortSecondTrackSchemeToMatch( Model modelA, Model modelB )
	{
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( modelA.getGraph(), modelB.getGraph() );
		FlipDescendants.flipDescendants( modelB, getSpotsToFlipB( result ) );
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
		tagBranches( modelA, LineageColoring.findTag( tagSet, notMapped ), getUnmatchSpotsA( result ) );
		tagBranches( modelA, LineageColoring.findTag( tagSet, flipped ), getSpotsToFlipA( result ) );
	}

	private static void tagBranches( Model model, TagSetStructure.Tag tag, Collection< Spot > branchStrats )
	{
		for ( Spot spot : branchStrats )
			tagBranch( tag, model, spot );
	}

	private static void tagBranch( TagSetStructure.Tag tag, Model model, Spot spot )
	{
		ModelGraph graphA = model.getGraph();
		Spot s = graphA.vertexRef();
		try
		{
			ObjTags< Spot > vertexTags = model.getTagSetModel().getVertexTags();
			ObjTags< Link > edgeTags = model.getTagSetModel().getEdgeTags();
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
