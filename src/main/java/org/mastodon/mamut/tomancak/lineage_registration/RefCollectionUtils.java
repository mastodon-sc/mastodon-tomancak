package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;

public class RefCollectionUtils
{

	public static < T > RefSet< T > filterSet( RefCollection< T > values, Predicate< T > predicate )
	{
		RefSet< T > filtered = RefCollections.createRefSet( values );
		for ( T t : values )
			if ( predicate.test( t ) )
				filtered.add( t );
		return filtered;
	}

	public static < T > RefSet< T > applySet( RefCollection< T > values, UnaryOperator< T > function )
	{
		RefSet< T > filtered = RefCollections.createRefSet( values );
		for ( T t : values )
			filtered.add( function.apply( t ) );
		return filtered;
	}

	public static < T > RefPool< T > getRefPool( RefCollection< T > collection )
	{
		RefPool< T > pool = RefCollections.tryGetRefPool( collection );
		if ( pool == null )
			throw new IllegalArgumentException( "Could not get RefPool from the given RefSet." );
		return pool;
	}
}
