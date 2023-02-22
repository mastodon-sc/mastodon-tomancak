package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import net.imglib2.realtransform.AffineTransform3D;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class LineageRegistrationAlgorithmTest
{

	@Test
	public void testRun()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( embryoA.graph, embryoB.graph );
		List< String > strings = asString( result.mapAB );
		assertEquals( Arrays.asList(
				"A -> A",
				"A1 -> A1",
				"A2 -> A2",
				"B -> B",
				"B1 -> B2",
				"B2 -> B1",
				"C -> C",
				"C1 -> C1",
				"C2 -> C2" ), strings );
	}

	@NotNull
	private static List< String > asString( RefRefMap< Spot, Spot > map )
	{
		List< String > strings = new ArrayList<>();
		RefMapUtils.forEach( map, ( a, b ) -> strings.add( a.getLabel() + " -> " + b.getLabel() ) );
		Collections.sort( strings );
		return strings;
	}

	@Test
	public void testSortSecondTrackSchemeToMatch()
	{
		// NB: The graphs need to have at least 3 dividing lineages.
		// Only the root nodes of the dividing lineages are used
		// to calculate the affine transform between the two "embryos".

		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();

		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.b1, firstChild( embryoB.graph, embryoB.b ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );

		LineageRegistrationUtils.sortSecondTrackSchemeToMatch( embryoA.model, embryoB.model );

		assertEquals( embryoB.a1, firstChild( embryoB.graph, embryoB.a ) );
		assertEquals( embryoB.c1, firstChild( embryoB.graph, embryoB.c ) );
		assertEquals( embryoB.b2, firstChild( embryoB.graph, embryoB.b ) );
	}

	@Test
	public void testTagCells()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();
		LineageRegistrationUtils.tagCells( embryoA.model, embryoB.model, true, true );
		assertEquals( set(), getTaggedSpots( embryoA.model, "registration", "not mapped" ) );
		assertEquals( set( "B", "B~1", "B~2" ), getTaggedSpots( embryoA.model, "registration", "flipped" ) );
		assertEquals( set(), getTaggedSpots( embryoB.model, "registration", "not mapped" ) );
		assertEquals( set( "B", "B~1", "B~2" ), getTaggedSpots( embryoB.model, "registration", "flipped" ) );
	}

	@Test
	public void testCopyTagSet()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();
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
		LineageRegistrationUtils.copyTagSetToSecond( embryoA.model, embryoB.model, tagSet );
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
			return LineageTreeUtils.getBranchEnd( tA, ref ).outgoingEdges().get( 0 ).getTarget();
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}

	private static Spot addSpot( ModelGraph graph, Spot parent, String label, double... position )
	{
		int t = parent == null ? 0 : parent.getTimepoint() + 1;
		Spot spot = graph.addVertex().init( t, position, 1 );
		spot.setLabel( label );
		if ( parent != null )
			graph.addEdge( parent, spot );
		return spot;
	}

	private static Spot addBranch( ModelGraph graph, Spot branchStart, int length )
	{
		String label = branchStart.getLabel();
		double[] position = { branchStart.getDoublePosition( 0 ), branchStart.getDoublePosition( 1 ), branchStart.getDoublePosition( 2 ) };
		Spot s = branchStart;
		for ( int i = 1; i < length; i++ )
			s = addSpot( graph, s, label + "~" + i, position );
		return s;
	}

	private static class EmbryoA
	{

		final Model model = new Model();

		final ModelGraph graph = model.getGraph();

		final Spot a = addSpot( graph, null, "A", 2, 2, 0 );

		final Spot aEnd = addBranch( graph, a, 2 );

		final Spot a1 = addSpot( graph, aEnd, "A1", 2, 1, 0 );

		final Spot a2 = addSpot( graph, aEnd, "A2", 2, 3, 0 );

		final Spot b = addSpot( graph, null, "B", 4, 2, 0 );

		final Spot bEnd = addBranch( graph, b, 3 );

		final Spot b1 = addSpot( graph, bEnd, "B1", 4, 1, 0 );

		final Spot b2 = addSpot( graph, bEnd, "B2", 4, 3, 0 );

		final Spot c = addSpot( graph, null, "C", 4, 4, 0 );

		final Spot c1 = addSpot( graph, c, "C1", 4, 4, 0 );

		final Spot c2 = addSpot( graph, c, "C2", 4, 5, 0 );
	}

	private static class EmbryoB extends EmbryoA
	{
		EmbryoB()
		{
			transformSpotPositions();
			flipB1andB2Positions();
		}

		private void transformSpotPositions()
		{
			// transform spot positions: rotate 90 degrees around x-axis
			AffineTransform3D transform = new AffineTransform3D();
			transform.rotate( 0, Math.PI / 2 );
			for ( Spot spot : graph.vertices() )
				transform.apply( spot, spot );
		}

		private void flipB1andB2Positions()
		{
			double[] v1 = new double[ 3 ];
			double[] v2 = new double[ 3 ];
			b1.localize( v1 );
			b2.localize( v2 );
			b1.setPosition( v2 );
			b2.setPosition( v1 );
		}
	}
}
