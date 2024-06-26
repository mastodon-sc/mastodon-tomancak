package org.mastodon.mamut.tomancak.resolve;

public class FuseSpotSelectionException extends RuntimeException
{
	public FuseSpotSelectionException()
	{
		super( "The algorithm can only merge parallel tracks. The given set of spots does not fulfill this requirement." );
	}
}
