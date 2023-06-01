package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;

public class RefCollectionUtils
{

	/**
	 * Returns a new {@link RefSet} containing all elements of the given
	 * {@link RefCollection} that satisfy the given {@link Predicate}.
	 */
	public static < T > RefSet< T > filterSet( RefCollection< T > values, Predicate< T > predicate )
	{
		RefSet< T > filtered = RefCollections.createRefSet( values );
		for ( T t : values )
			if ( predicate.test( t ) )
				filtered.add( t );
		return filtered;
	}

	/**
	 * Applies the given {@code function} to all elements of the given
	 * {@link RefCollection} and returns a new {@link RefSet} containing the
	 * results.
	 */
	public static < T > RefSet< T > applySet( RefCollection< T > values, UnaryOperator< T > function )
	{
		RefSet< T > filtered = RefCollections.createRefSet( values );
		for ( T t : values )
			filtered.add( function.apply( t ) );
		return filtered;
	}

	/**
	 * Extracts the {@link RefPool} from a given {@link RefCollection}.
	 * <p>
	 * Similar to {@link RefCollections#tryGetRefPool(RefCollection)}
	 * but throws an exception instead of returning "null" in case of
	 * a failure.
	 *
	 * @see RefCollections#tryGetRefPool(RefCollection)
	 * @throws IllegalArgumentException if that fails.
	 */
	public static < T > RefPool< T > getRefPool( RefCollection< T > collection )
	{
		RefPool< T > pool = RefCollections.tryGetRefPool( collection );
		if ( pool == null )
			throw new IllegalArgumentException( "Could not get RefPool from the given RefCollection. Collection class: "
					+ collection.getClass() );
		return pool;
	}
}
