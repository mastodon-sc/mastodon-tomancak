package org.mastodon.mamut.tomancak.collaboration.sshauthentication;

import java.util.Scanner;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * A {@link CredentialsProvider} that allows to answer yes/no questions
 * interactively on the command line.
 */
public class CustomCredentialsProvider extends CredentialsProvider
{

	@Override
	public boolean isInteractive()
	{
		return true;
	}

	@Override
	public boolean supports( CredentialItem... items )
	{
		return true;
	}

	@Override
	public boolean get( URIish uri, CredentialItem... items ) throws UnsupportedCredentialItem
	{
		boolean ok = true;
		for ( CredentialItem item : items )
			ok &= processItem( item );
		if ( !ok )
			throw new UnsupportedOperationException();
		return ok;
	}

	private boolean processItem( CredentialItem item )
	{
		if ( item instanceof CredentialItem.InformationalMessage )
			return processInformalMessage( ( CredentialItem.InformationalMessage ) item );
		if ( item instanceof CredentialItem.YesNoType )
			return processYesNo( ( CredentialItem.YesNoType ) item );
		return false;
	}

	private boolean processInformalMessage( CredentialItem.InformationalMessage item )
	{
		System.out.println( item.getPromptText() );
		return true;
	}

	private boolean processYesNo( CredentialItem.YesNoType item )
	{
		System.out.println( item.getPromptText() + " (yes/no)" );
		String line = new Scanner( System.in ).nextLine();
		item.setValue( "yes".equals( line ) );
		return true;
	}
}

