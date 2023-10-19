package org.mastodon.mamut.tomancak.collaboration.exceptions;

public class GraphMergeException extends RuntimeException
{

	public GraphMergeException( final String message )
	{
		super( message );
	}

	public GraphMergeException( String message, final Throwable cause )
	{
		super( message, cause );
	}
}
