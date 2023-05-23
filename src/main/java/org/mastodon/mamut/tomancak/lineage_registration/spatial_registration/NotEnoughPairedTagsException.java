package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

public class NotEnoughPairedTagsException extends RuntimeException
{

	public NotEnoughPairedTagsException( String message ) {
		super( message );
	}
}
