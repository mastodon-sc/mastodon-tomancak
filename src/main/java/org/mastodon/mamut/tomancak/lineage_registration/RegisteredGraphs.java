package org.mastodon.mamut.tomancak.lineage_registration;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.RefPool;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * <p>
 * This datastructure holds two {@link ModelGraph}s and a mapping between them.
 * </p>
 * <p>
 * This is also the return type of {@link LineageRegistrationAlgorithm#run(ModelGraph, ModelGraph)}.
 * </p>
 */
public class RegisteredGraphs
{

	public final ModelGraph graphA;

	public final ModelGraph graphB;

	/** A transformation that transforms coordinates in graph A to coordinates in graph B. */
	public final AffineTransform3D transformAB;

	/** Maps branch starting spot in graph A to corresponding branch starting spot in graph B. */
	public final RefRefMap< Spot, Spot > mapAB;

	/**
	 * <p>
	 * Maps branch starting spot in graph B to corresponding branch starting spot in graph A.
	 * </p>
	 * <p>
	 * Inverse map of {@link #mapAB}.
	 * </p>
	 */
	public final RefRefMap< Spot, Spot > mapBA;

	public RegisteredGraphs( ModelGraph graphA, ModelGraph graphB, AffineTransform3D transformAB, RefRefMap< Spot, Spot > mapAB )
	{
		this.transformAB = transformAB;
		this.graphA = graphA;
		this.graphB = graphB;
		this.mapAB = mapAB;
		this.mapBA = invertRefRefMap( mapAB, graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
	}

	public RegisteredGraphs( ModelGraph graphA, ModelGraph graphB,
			AffineTransform3D transformAB,
			RefRefMap< Spot, Spot > mapAB, RefRefMap< Spot, Spot > mapBA )
	{
		this.transformAB = transformAB;
		this.graphA = graphA;
		this.graphB = graphB;
		this.mapAB = mapAB;
		this.mapBA = mapBA;
	}

	private static < K, V > RefRefMap< V, K > invertRefRefMap( RefRefMap< K, V > map, RefPool< K > keysPool, RefPool< V > valuesPool )
	{
		RefRefMap< V, K > inverted = new RefRefHashMap<>( valuesPool, keysPool );
		RefMapUtils.forEach( map, ( k, v ) -> inverted.put( v, k ) );
		return inverted;
	}

	/**
	 * @return a {@link RegisteredGraphs} instance where graph A and graph B are swapped. And all mappings are inverted.
	 */
	public RegisteredGraphs swapAB()
	{
		return new RegisteredGraphs( this.graphB, this.graphA, this.transformAB.inverse(), this.mapBA, this.mapAB );
	}
}
