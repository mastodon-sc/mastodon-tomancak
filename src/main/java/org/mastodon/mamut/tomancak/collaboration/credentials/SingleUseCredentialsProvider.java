package org.mastodon.mamut.tomancak.collaboration.credentials;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * The JGIT api does not tell a CredentialsProvider if the credentials
 * where correct. It simply asks for them again if they were wrong.
 * <p>
 * We can exploit this behavior by counting the number of times the
 * CredentialsProvider was asked for credentials. If it was asked more than
 * once, we assume that the credentials were wrong.
 * <p>
 * This only works if the CredentialsProvider is only used once.
 */
class SingleUseCredentialsProvider extends CredentialsProvider
{
	private final Callback callback;

	private int counter = 0;

	public SingleUseCredentialsProvider( Callback callback )
	{
		this.callback = callback;
	}

	@Override
	public boolean isInteractive()
	{
		return true;
	}

	@Override
	public boolean supports( CredentialItem... items )
	{
		for ( CredentialItem item : items )
			if ( !isUsernameOrPassword( item ) )
				return false;
		return true;
	}

	private boolean isUsernameOrPassword( CredentialItem item )
	{
		return ( item instanceof CredentialItem.Username ) || ( item instanceof CredentialItem.Password );
	}

	@Override
	public boolean get( URIish uri, CredentialItem... items ) throws UnsupportedCredentialItem
	{
		if ( !supports( items ) )
			throw new UnsupportedCredentialItem( uri, "" );
		counter++;
		boolean previousAuthenticationFailed = counter > 1;
		Pair< String, String > usernameAndPassword = callback.getUsernameAndPassword( uri, previousAuthenticationFailed );
		if ( usernameAndPassword == null )
			return false;
		fillUsernameAndPassword( items, usernameAndPassword.getLeft(), usernameAndPassword.getRight() );
		return true;
	}

	private void fillUsernameAndPassword( CredentialItem[] items, String username, String password )
	{
		for ( CredentialItem item : items )
			fillItem( item, username, password );
	}

	private void fillItem( CredentialItem item, String username, String password )
	{
		if ( item instanceof CredentialItem.Username )
			( ( CredentialItem.Username ) item ).setValue( username );
		else if ( item instanceof CredentialItem.Password )
			( ( CredentialItem.Password ) item ).setValue( password.toCharArray() );
	}

	/**
	 * Callback interface for {@link SingleUseCredentialsProvider}.
	 */
	interface Callback
	{

		/**
		 * The {@link SingleUseCredentialsProvider} calls this method to get
		 * username and password for the given {@link URIish}.
		 *
		 * @param uri                   the URI for which credentials are requested.
		 * @param authenticationFailure true if the SingleUseCredentialsProvider
		 *                              thinks that the credentials were wrong.
		 * @return username and password or null if the user canceled the
		 * request.
		 * @see SingleUseCredentialsProvider
		 */
		Pair< String, String > getUsernameAndPassword( URIish uri, boolean authenticationFailure );
	}
}
