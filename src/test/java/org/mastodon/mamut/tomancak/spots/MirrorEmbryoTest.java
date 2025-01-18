/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.spots;

import static org.junit.Assert.assertArrayEquals;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Tests {@link MirrorEmbryo}.
 */
public class MirrorEmbryoTest
{
	@Test
	public void testMirrorSimpleGraph()
	{
		// setup
		ModelGraph graph = new ModelGraph();
		double[][] covariance1 = { { 1, 0, 0 }, { 0, 2, 0 }, { 0, 0, 3 } };
		Spot spot1 = graph.addVertex().init( 0, new double[] { 1, 2, 3 }, covariance1 );
		double[][] covariance2 = { { 1, 0.2, 0.2 }, { 0.2, 2, 0.2 }, { 0.2, 0.2, 3 } };
		Spot spot2 = graph.addVertex().init( 1, new double[] { 4, 5, 6 }, covariance2 );
		// process
		MirrorEmbryo.mirrorX( graph );
		// test
		assertArrayEquals( new double[] { 4, 2, 3 }, spot1.positionAsDoubleArray(), 0.0 );
		assertArrayEquals( new double[] { 1, 5, 6 }, spot2.positionAsDoubleArray(), 0.0 );
		assertArrayEquals( new double[] { 1, 0, 0, 0, 2, 0, 0, 0, 3 }, getFlattenedCovariance( spot1 ), 0.0 );
		assertArrayEquals( new double[] { 1, -0.2, -0.2, -0.2, 2, 0.2, -0.2, 0.2, 3 }, getFlattenedCovariance( spot2 ), 0.0 );
	}

	/**
	 * Returns the covariance matrix of the given spot as a flat double[9] array.
	 */
	private double[] getFlattenedCovariance( Spot spot )
	{
		double[][] covariance = new double[ 3 ][ 3 ];
		spot.getCovariance( covariance );
		// Convert double[3][3] to to flat double[9] array
		return Stream.of( covariance ).flatMapToDouble( DoubleStream::of ).toArray();
	}

}
