package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.BiConsumer;

import org.mastodon.collection.ObjectRefMap;

public class RefMapUtils
{
	// TODO: This needs to become the default implementation of ObjectRefMap.forEach().
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
