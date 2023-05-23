package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

public class NotEnoughPairedRootsException extends RuntimeException
{

	public NotEnoughPairedRootsException() {
		super( "At least 3 paired roots are needed to compute a coordinate transformation between the two datasets." );
	}

	public NotEnoughPairedRootsException( String message ) {
		super( message );
	}
}
