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
