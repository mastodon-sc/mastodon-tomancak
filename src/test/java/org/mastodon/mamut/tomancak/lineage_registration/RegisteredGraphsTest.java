/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistration;

public class RegisteredGraphsTest
{

	private Model modelA;

	private Spot spotA1;

	private Spot spotA2;

	private Model modelB;

	private Spot spotB1;

	private Spot spotB2;

	private RefRefMap< Spot, Spot > mapAB;

	private RefDoubleMap< Spot > anglesA;

	private SpatialRegistration spatialRegistration;

	private RegisteredGraphs registeredGraphs;

	@Before
	public void setup() {
		modelA = new Model();
		spotA1 = addSpot( modelA );
		spotA2 = addSpot( modelA );
		modelB = new Model();
		spotB1 = addSpot( modelB );
		spotB2 = addSpot( modelB );
		mapAB = new RefRefHashMap<>( modelA.getGraph().vertices().getRefPool(), modelB.getGraph().vertices().getRefPool() );
		mapAB.put( spotA1, spotB1 );
		mapAB.put( spotA2, spotB2 );
		anglesA = new RefDoubleHashMap<>( modelA.getGraph().vertices().getRefPool(), Double.NaN );
		anglesA.put( spotA1, 30 );
		spatialRegistration = ( timepointA, timepointB ) -> new AffineTransform3D();
		registeredGraphs = new RegisteredGraphs( modelA, modelB, spatialRegistration, mapAB, anglesA );
	}

	private static Spot addSpot( Model modelA )
	{
		return modelA.getGraph().addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
	}

	@Test
	public void testSimpleFields()
	{
		assertSame( modelA, registeredGraphs.modelA );
		assertSame( modelB, registeredGraphs.modelB );
		assertSame( modelA.getGraph(), registeredGraphs.graphA );
		assertSame( modelB.getGraph(), registeredGraphs.graphB );
		assertSame( mapAB, registeredGraphs.mapAB );
		assertSame( anglesA, registeredGraphs.anglesA );
		assertSame( spatialRegistration, registeredGraphs.spatialRegistration );
	}

	@Test
	public void testMapBA()
	{
		RefRefMap< Spot, Spot > mapBA = registeredGraphs.mapBA;
		assertEquals( spotA1, mapBA.get( spotB1 ) );
		assertEquals( spotA2, mapBA.get( spotB2 ) );
	}

	@Test
	public void testAnglesB()
	{
		assertEquals( 30, registeredGraphs.anglesB.get( spotB1 ), 0.0 );
		assertFalse( registeredGraphs.anglesB.containsKey( spotB2 ) );
	}

	@Test
	public void testSwapAB()
	{
		RegisteredGraphs swapped = registeredGraphs.swapAB();
		assertSame( registeredGraphs.modelB, swapped.modelA );
		assertSame( registeredGraphs.modelA, swapped.modelB );
		assertSame( registeredGraphs.graphB, swapped.graphA );
		assertSame( registeredGraphs.graphA, swapped.graphB );
		assertSame( registeredGraphs.mapBA, swapped.mapAB );
		assertSame( registeredGraphs.mapAB, swapped.mapBA );
		assertSame( registeredGraphs.anglesB, swapped.anglesA );
		assertSame( registeredGraphs.anglesA, swapped.anglesB );
	}
}
