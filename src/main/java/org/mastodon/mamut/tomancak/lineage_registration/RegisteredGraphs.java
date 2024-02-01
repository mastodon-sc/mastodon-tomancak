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

import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.InverseSpatialRegistration;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistration;

/**
 * <p>
 * This datastructure holds two {@link ModelGraph}s and a mapping between them.
 * </p>
 * <p>
 * This is also the return type of {@link LineageRegistrationAlgorithm#run}.
 * </p>
 */
public class RegisteredGraphs
{
	public final Model modelA;

	public final Model modelB;

	public final ModelGraph graphA;

	public final ModelGraph graphB;

	/** A transformation that transforms coordinates in graph A to coordinates in graph B. */
	public final SpatialRegistration spatialRegistration;

	/** Maps branch starting spot in graph A to corresponding branch starting spot in graph B. */
	public final RefRefMap< Spot, Spot > mapAB;

	/**
	 * Maps branch starting spot in graph B to corresponding branch starting spot in graph A.
	 * <p>
	 * Inverse map of {@link #mapAB}.
	 */
	public final RefRefMap< Spot, Spot > mapBA;

	public final RefDoubleMap< Spot > anglesA;

	public final RefDoubleMap< Spot > anglesB;

	public RegisteredGraphs( Model modelA, Model modelB, SpatialRegistration spatialRegistration, RefRefMap< Spot, Spot > mapAB, RefDoubleMap< Spot > anglesA )
	{
		this( modelA, modelB, spatialRegistration,
				mapAB, invertRefRefMap( mapAB, spotRefPool( modelA ), spotRefPool( modelB ) ),
				anglesA, computeAnglesB( mapAB, anglesA, spotRefPool( modelB ) ) );
	}

	private RegisteredGraphs( Model modelA, Model modelB,
			SpatialRegistration spatialRegistration,
			RefRefMap< Spot, Spot > mapAB,
			RefRefMap< Spot, Spot > mapBA,
			RefDoubleMap< Spot > anglesA,
			RefDoubleMap< Spot > anglesB )
	{
		this.spatialRegistration = spatialRegistration;
		this.modelA = modelA;
		this.modelB = modelB;
		this.graphA = modelA.getGraph();
		this.graphB = modelB.getGraph();
		this.mapAB = mapAB;
		this.mapBA = mapBA;
		this.anglesA = anglesA;
		this.anglesB = anglesB;
	}

	/**
	 * @return a {@link RegisteredGraphs} instance where graph A and graph B are swapped. And all mappings are inverted.
	 */
	public RegisteredGraphs swapAB()
	{
		return new RegisteredGraphs( this.modelB, this.modelA, new InverseSpatialRegistration( this.spatialRegistration ), this.mapBA,
				this.mapAB, this.anglesB, this.anglesA );
	}

	private static < K, V > RefRefMap< V, K > invertRefRefMap( RefRefMap< K, V > map, RefPool< K > keysPool, RefPool< V > valuesPool )
	{
		RefRefMap< V, K > inverted = new RefRefHashMap<>( valuesPool, keysPool );
		RefMapUtils.forEach( map, ( k, v ) -> inverted.put( v, k ) );
		return inverted;
	}

	private static RefDoubleMap< Spot > computeAnglesB( RefRefMap< Spot, Spot > mapAB, RefDoubleMap< Spot > anglesA, RefPool< Spot > refPoolB )
	{
		final Spot refB = refPoolB.createRef();
		try
		{
			final RefDoubleMap< Spot > anglesB = new RefDoubleHashMap<>( refPoolB, Double.NaN );
			for ( Spot spotA : anglesA.keySet() )
			{
				final Spot spotB = mapAB.get( spotA, refB );
				anglesB.put( spotB, anglesA.get( spotA ) );
			}
			return anglesB;
		}
		finally
		{
			refPoolB.releaseRef( refB );
		}
	}

	private static RefPool< Spot > spotRefPool( Model model )
	{
		return model.getGraph().vertices().getRefPool();
	}
}
