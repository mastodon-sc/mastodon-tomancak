package org.mastodon.mamut.tomancak.lineage_registration.angle_feature;

import java.util.Collections;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.properties.DoublePropertyMap;

public abstract class BranchScalarFeature< BV extends Vertex< ? >, V extends Vertex< ? > > implements Feature< BV >
{

	private final FeatureProjectionKey key;

	private final DoublePropertyMap< V > values;

	private final String units;

	private final BranchGraph< BV, ?, V, ? > branchGraph;

	private final RefPool< V > pool;

	private final V ref;

	public BranchScalarFeature(
			final String key,
			final Dimension dimension,
			final String units,
			final BranchGraph< BV, ?, V, ? > branchGraph,
			final RefPool< V > pool )
	{
		final FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( key, dimension );
		this.key = FeatureProjectionKey.key( projectionSpec );
		this.units = units;
		this.branchGraph = branchGraph;
		this.pool = pool;
		this.values = new DoublePropertyMap<>( pool, Double.NaN );
		this.ref = pool.createRef();
	}

	@Override
	public abstract FeatureSpec< ? extends Feature< BV >, BV > getSpec();

	private V toVertex( final BV bv )
	{
		return branchGraph.getLastLinkedVertex( bv, ref );
	}

	public boolean isSet( final BV bv )
	{
		return values.isSet( toVertex( bv ) );
	}

	public double value( final BV bv )
	{
		return values.getDouble( toVertex( bv ) );
	}

	public void set( final BV bv, final double value )
	{
		values.set( toVertex( bv ), value );
	}

	@Override
	public void invalidate( final BV bv )
	{
		values.remove( toVertex( bv ) );
	}

	@Override
	public FeatureProjection< BV > project( final FeatureProjectionKey key )
	{
		if ( this.key.equals( key ) )
			return new BranchAdaptingFeatureProjection( pool.createRef() );

		return null;
	}

	@Override
	public Set< FeatureProjection< BV > > projections()
	{
		return Collections.singleton( new BranchAdaptingFeatureProjection( pool.createRef() ) );
	}

	public class BranchAdaptingFeatureProjection implements FeatureProjection< BV >
	{

		private final V ref;

		public BranchAdaptingFeatureProjection( final V ref )
		{
			this.ref = ref;
		}

		private V toVertex( final BV bv )
		{
			return branchGraph.getLastLinkedVertex( bv, ref );
		}

		public void set( final BV bv, final double value )
		{
			values.set( toVertex( bv ), value );
		}

		public void remove( final BV bv )
		{
			values.remove( toVertex( bv ) );
		}

		@Override
		public boolean isSet( final BV bv )
		{
			return values.isSet( toVertex( bv ) );
		}

		@Override
		public double value( final BV bv )
		{
			return values.getDouble( toVertex( bv ) );
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key;
		}

		@Override
		public String units()
		{
			return units;
		}
	}
}
