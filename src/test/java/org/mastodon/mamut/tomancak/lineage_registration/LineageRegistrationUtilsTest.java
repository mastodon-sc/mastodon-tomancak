package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class LineageRegistrationUtilsTest
{
	private EmbryoA embryoA;

	private EmbryoB embryoB;

	private RegisteredGraphs registration;

	@Before
	public void before()
	{
		embryoA = new EmbryoA();
		embryoB = new EmbryoB();
		// NB: The graphs need to have at least 3 dividing lineages.
		// Only the root nodes of the dividing lineages are used
		// to calculate the affine transform between the two "embryos".
		registration = LineageRegistrationAlgorithm.run(
				embryoA.model.getGraph(), 0,
				embryoB.model.getGraph(), 0 );
	}

	@Test
	public void testSortSecondTrackSchemeToMatch()
	{
		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.b1, firstChild( embryoB.graph, embryoB.b ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );
		LineageRegistrationUtils.sortSecondTrackSchemeToMatch( embryoA.model, embryoB.model, registration );
		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );
		assertEquals( embryoB.b2, firstChild( embryoB.graph, embryoB.b ) );
	}

	@Test
	public void testTagCells()
	{
		LineageRegistrationUtils.tagCells( embryoA.model, embryoB.model, registration, true, true );
		assertEquals( Collections.emptySet(), getTaggedSpots( embryoA.model, "lineage registration", "not mapped" ) );
		assertEquals( set( "B1", "B2" ), getTaggedSpots( embryoA.model, "lineage registration", "flipped" ) );
		assertEquals( Collections.emptySet(), getTaggedSpots( embryoB.model, "lineage registration", "not mapped" ) );
		assertEquals( set( "B1", "B2" ), getTaggedSpots( embryoB.model, "lineage registration", "flipped" ) );
	}

	@Test
	public void testCopyTagSet()
	{
		// setup: tag set for embryoA
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( embryoA.model, "test", Arrays.asList(
				Pair.of( "foo", 0xffff0000 ),
				Pair.of( "bar", 0xff00ff00 )
		) );
		TagSetStructure.Tag foo = tagSet.getTags().get( 0 );
		TagSetStructure.Tag bar = tagSet.getTags().get( 1 );
		TagSetUtils.tagBranch( embryoA.model, tagSet, foo, embryoA.a );
		TagSetUtils.tagBranch( embryoA.model, tagSet, foo, embryoA.a1 );
		TagSetUtils.tagBranch( embryoA.model, tagSet, foo, embryoA.a2 );
		TagSetUtils.tagBranch( embryoA.model, tagSet, bar, embryoA.b1 );
		embryoA.model.getTagSetModel().getEdgeTags().set( embryoA.model.getGraph().getEdge( embryoA.bEnd, embryoA.b1 ), bar );
		// process
		LineageRegistrationUtils.copyTagSetToSecond( embryoA.model, embryoB.model, registration, tagSet, "test" );
		// test: tag set for embryoB
		assertEquals( set( "A", "A~1", "A1", "A2" ), getTaggedSpots( embryoB.model, "test", "foo" ) );
		assertEquals( set( "B2" ), getTaggedSpots( embryoB.model, "test", "bar" ) );
		assertEquals( set( "B~2 -> B2" ), getTaggedEdges( embryoB.model, "test", "bar" ) );
	}

	private static < T > Set< T > set( T... values )
	{
		return new HashSet<>( Arrays.asList( values ) );
	}

	private static Set< String > getTaggedSpots( Model model, String tagSetName, String tagLabel )
	{
		TagSetModel< Spot, Link > tagsModel = model.getTagSetModel();
		TagSetStructure.TagSet tagSet = findTagSet( tagsModel, tagSetName );
		TagSetStructure.Tag tag = findTag( tagSet, tagLabel );
		return tagsModel.getVertexTags().getTaggedWith( tag ).stream().map( Spot::getLabel ).collect( Collectors.toSet() );
	}

	private static Set< String > getTaggedEdges( Model model, String tagSetName, String tagLabel )
	{
		TagSetModel< Spot, Link > tagsModel = model.getTagSetModel();
		TagSetStructure.TagSet tagSet = findTagSet( tagsModel, tagSetName );
		TagSetStructure.Tag tag = findTag( tagSet, tagLabel );
		Collection< Link > edges = tagsModel.getEdgeTags().getTaggedWith( tag );
		HashSet< String > strings = new HashSet<>();
		for ( Link edge : edges )
			strings.add( edge.getSource().getLabel() + " -> " + edge.getTarget().getLabel() );
		return strings;
	}

	private static TagSetStructure.TagSet findTagSet( TagSetModel< Spot, Link > tagsModel, String name )
	{
		for ( TagSetStructure.TagSet tagSet : tagsModel.getTagSetStructure().getTagSets() )
			if ( name.equals( tagSet.getName() ) )
				return tagSet;
		throw new NoSuchElementException();
	}

	public static TagSetStructure.Tag findTag( TagSetStructure.TagSet tagSet, String label )
	{
		for ( TagSetStructure.Tag tag : tagSet.getTags() )
			if ( label.equals( tag.label() ) )
				return tag;
		throw new NoSuchElementException();
	}

	private Spot firstChild( ModelGraph graph, Spot tA )
	{
		Spot ref = graph.vertexRef();
		try
		{
			return BranchGraphUtils.getBranchEnd( tA, ref ).outgoingEdges().get( 0 ).getTarget();
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}
}
