package org.mastodon.mamut.tomancak.lineage_registration;


import org.mastodon.RefPool;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefRefHashMap;
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

	public final ModelGraph graphA;

	public final ModelGraph graphB;

	/** A transformation that transforms coordinates in graph A to coordinates in graph B. */
	public final SpatialRegistration spatialRegistration;

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

	public RegisteredGraphs( ModelGraph graphA, ModelGraph graphB, SpatialRegistration spatialRegistration, RefRefMap< Spot, Spot > mapAB )
	{
		this.spatialRegistration = spatialRegistration;
		this.graphA = graphA;
		this.graphB = graphB;
		this.mapAB = mapAB;
		this.mapBA = invertRefRefMap( mapAB, graphA.vertices().getRefPool(), graphB.vertices().getRefPool() );
	}

	private RegisteredGraphs( ModelGraph graphA, ModelGraph graphB,
			SpatialRegistration spatialRegistration,
			RefRefMap< Spot, Spot > mapAB, RefRefMap< Spot, Spot > mapBA )
	{
		this.spatialRegistration = spatialRegistration;
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
		return new RegisteredGraphs( this.graphB, this.graphA, new InverseSpatialRegistration( this.spatialRegistration ), this.mapBA,
				this.mapAB );
	}
}
