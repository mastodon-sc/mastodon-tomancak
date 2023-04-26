package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Spot;

public class LineageRegistrationAlgorithmTest
{

	List< String > expected = Arrays.asList(
			"A -> A",
			"A1 -> A1",
			"A2 -> A2",
			"B -> B",
			"B1 -> B2",
			"B2 -> B1",
			"C -> C",
			"C1 -> C1",
			"C2 -> C2" );

	@Test
	public void testRun()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( embryoA.graph, 0, embryoB.graph, 0 );
		assertEquals( expected, asStrings( result.mapAB ) );
	}

	@Test
	public void testDifferentlyStagedEmbryos()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoBSingleCellStage embryoB = new EmbryoBSingleCellStage();
		RegisteredGraphs result = LineageRegistrationAlgorithm.run( embryoA.graph, 0, embryoB.graph, 2 );
		assertEquals( expected, asStrings( result.mapAB ) );
		assertEquals( embryoB.beforeA, result.mapAB.get( embryoA.a ) );
		assertTransformEquals( embryoB.transform, result.transformAB );
	}

	private void assertTransformEquals( AffineTransform3D expected, AffineTransform3D actual )
	{
		double[] expectedValues = new double[ 12 ];
		double[] actualValues = new double[ 12 ];
		expected.toArray( expectedValues );
		actual.toArray( actualValues );
		assertArrayEquals( expectedValues, actualValues, 0.01 );
	}

	private static List< String > asStrings( RefRefMap< Spot, Spot > map )
	{
		List< String > strings = new ArrayList<>();
		RefMapUtils.forEach( map, ( a, b ) -> strings.add( a.getLabel() + " -> " + b.getLabel() ) );
		Collections.sort( strings );
		return strings;
	}
}
