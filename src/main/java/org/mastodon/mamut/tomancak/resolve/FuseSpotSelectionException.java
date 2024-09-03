package org.mastodon.mamut.tomancak.resolve;

import java.util.Collection;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

/**
 * Exception that is thrown by {@link FuseSpots#run(Model, Collection, Spot)} whenever
 * the set of spots doesn't fulfill the very specific requirements of the algorithm. The
 * exception is meant to be caught and communicated to the user.
 */
public class FuseSpotSelectionException extends RuntimeException
{
	public FuseSpotSelectionException()
	{
		super( "The algorithm can only merge parallel tracks. The given set of spots does not fulfill this requirement." );
	}
}
