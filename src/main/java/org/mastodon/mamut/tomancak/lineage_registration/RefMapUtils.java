package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.BiConsumer;

import org.mastodon.collection.ObjectRefMap;

public class RefMapUtils
{
	// TODO: Replace with {@link ObjectRefMap#forEach} once the PR is merged, released and on the update site.
	// see https://github.com/mastodon-sc/mastodon-collection/pull/13.
	public static < K, V > void forEach( ObjectRefMap< K, V > map, BiConsumer< K, V > action )
	{
		V ref = map.createValueRef();
		try
		{
			for ( K key : map.keySet() )
			{
				V value = map.get( key, ref );
				action.accept( key, value );
			}
		}
		finally
		{
			map.releaseValueRef( ref );
		}
	}
}
