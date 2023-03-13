package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Spot;

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

	private static List< String > asString( RefRefMap< Spot, Spot > map )
	{
		List< String > strings = new ArrayList<>();
		RefMapUtils.forEach( map, ( a, b ) -> strings.add( a.getLabel() + " -> " + b.getLabel() ) );
		Collections.sort( strings );
		return strings;
	}
}
