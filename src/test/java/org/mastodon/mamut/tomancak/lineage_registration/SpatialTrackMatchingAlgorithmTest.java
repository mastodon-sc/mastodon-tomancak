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
package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Test;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistrationMethod;

public class SpatialTrackMatchingAlgorithmTest
{

	private final List< String > expected = Arrays.asList(
			"A -> A",
			"A1 -> A1",
			"A2 -> A2",
			"B -> B",
			"B1 -> B2",
			"B2 -> B1",
			"C -> C",
			"C1 -> C1",
			"C2 -> C2" );

	private final List< String > expectedAngles = Arrays.asList(
			"A -> 0.0",
			"B -> 180.0",
			"C -> 0.0" );

	@Test
	public void testRun()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoB embryoB = new EmbryoB();
		RegisteredGraphs result = SpatialTrackMatchingAlgorithm.run( embryoA.model, 0, embryoB.model, 0,
				SpatialRegistrationMethod.FIXED_ROOTS );
		assertEquals( expected, asStrings( result.mapAB ) );
		assertEquals( expectedAngles, asStrings( result.anglesA ) );
	}

	@Test
	public void testDifferentlyStagedEmbryos()
	{
		EmbryoA embryoA = new EmbryoA();
		EmbryoBSingleCellStage embryoB = new EmbryoBSingleCellStage();
		RegisteredGraphs result = SpatialTrackMatchingAlgorithm.run( embryoA.model, 0, embryoB.model, 2,
				SpatialRegistrationMethod.FIXED_ROOTS );
		assertEquals( expected, asStrings( result.mapAB ) );
		assertEquals( embryoB.beforeA, result.mapAB.get( embryoA.a ) );
		assertTransformEquals( embryoB.transform, result.spatialRegistration.getTransformationAtoB( 0, 2 ) );
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

	private static List< String > asStrings( RefDoubleMap< Spot > map )
	{
		List< String > strings = new ArrayList<>();
		map.forEachEntry( ( a, b ) -> strings.add( a.getLabel() + " -> " + b ) );
		Collections.sort( strings );
		return strings;
	}
}
