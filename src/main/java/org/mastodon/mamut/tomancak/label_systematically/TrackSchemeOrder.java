package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.Spot;

import java.util.function.Predicate;

/**
 * Returns if the first child of the given spot is left if the TrackScheme.
 * (This is by definition always true)
 */
public class TrackSchemeOrder implements Predicate<Spot>
{
	@Override
	public boolean test( Spot spot )
	{
		return true;
	}
}
