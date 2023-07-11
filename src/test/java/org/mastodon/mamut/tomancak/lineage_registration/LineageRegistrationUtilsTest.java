package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistrationMethod;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagHelper;
import org.mastodon.util.TagSetUtils;

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
				embryoA.model, 0,
				embryoB.model, 0,
				SpatialRegistrationMethod.FIXED_ROOTS );
	}

	@Test
	public void testSortSecondTrackSchemeToMatch()
	{
		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.b1, firstChild( embryoB.graph, embryoB.b ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );
		LineageRegistrationUtils.sortSecondTrackSchemeToMatch( registration );
		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );
		assertEquals( embryoB.b2, firstChild( embryoB.graph, embryoB.b ) );
	}

	@Test
	public void testTagCells()
	{
		LineageRegistrationUtils.tagCells( registration, true, true );
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
		TagHelper foo = new TagHelper( embryoA.model, tagSet, "foo" );
		TagHelper bar = new TagHelper( embryoA.model, tagSet, "bar" );
		foo.tagBranch( embryoA.a );
		foo.tagBranch( embryoA.a1 );
		foo.tagBranch( embryoA.a2 );
		bar.tagBranch( embryoA.b1 );
		bar.tagLink( embryoA.model.getGraph().getEdge( embryoA.bEnd, embryoA.b1 ) );
		// process
		LineageRegistrationUtils.copyTagSetToSecondModel( registration, tagSet, "new-tag-set" );
		// test: tag set for embryoB
		TagHelper fooB = new TagHelper( embryoB.model, "new-tag-set", "foo" );
		TagHelper barB = new TagHelper( embryoB.model, "new-tag-set", "bar" );
		assertEquals( set( "A", "A~1", "A1", "A2" ), getTaggedSpots( fooB ) );
		assertEquals( set( "B2" ), getTaggedSpots( barB ) );
		assertEquals( set( "B~2 -> B2" ), getTaggedEdges( barB ) );
	}

	private static < T > Set< T > set( T... values )
	{
		return new HashSet<>( Arrays.asList( values ) );
	}

	private static Set< String > getTaggedSpots( Model model, String tagSetName, String tagLabel )
	{
		return getTaggedSpots( new TagHelper( model, tagSetName, tagLabel ) );
	}

	private static Set< String > getTaggedSpots( TagHelper tag )
	{
		return tag.getTaggedSpots().stream()
				.map( Spot::getLabel )
				.collect( Collectors.toSet() );
	}

	private static Set< String > getTaggedEdges( TagHelper tag )
	{
		return tag.getTaggedLinks().stream()
				.map( edge -> edge.getSource().getLabel() + " -> " + edge.getTarget().getLabel() )
				.collect( Collectors.toSet() );
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
