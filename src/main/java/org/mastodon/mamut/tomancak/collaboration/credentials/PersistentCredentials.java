package org.mastodon.mamut.tomancak.collaboration.credentials;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

public class PersistentCredentials
{

	private String username = null;

	private String password = null;

	private boolean missingCredentials()
	{
		return password == null || username == null;
	}

	private boolean queryPassword( String url, boolean previousAuthenticationFailed )
	{
		JTextField usernameField = new JTextField( 20 );
		JPasswordField passwordField = new JPasswordField( 20 );

		final JPanel panel = new JPanel();
		panel.setLayout( new MigLayout( "insets dialog" ) );
		panel.add( new JLabel( "Please enter your credentials for the Git repository:" ), "span, wrap" );
		panel.add( new JLabel( url ), "span, wrap, gapbottom unrelated" );
		panel.add( new JLabel( "username" ) );
		panel.add( usernameField, "wrap" );
		panel.add( new JLabel( " password" ) );
		panel.add( passwordField, "wrap" );
		if ( previousAuthenticationFailed )
			panel.add( new JLabel( "<html><font color=red>(Authentication failed. Please try again!)" ), "span, wrap" );
		boolean ok = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog( null,
				panel, "Authentication for Git Repository",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
		if ( !ok )
			return false;

		username = usernameField.getText();
		password = new String( passwordField.getPassword() );
		return true;
	}

	public CredentialsProvider getSingleUseCredentialsProvider()
	{
		return new SingleUseCredentialsProvider();
	}

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
	private class SingleUseCredentialsProvider extends CredentialsProvider
	{
		private int counter = 0;

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
				return false;
			counter++;
			boolean previousAuthenticationFailed = counter > 1;
			if ( previousAuthenticationFailed || missingCredentials() )
				if ( !queryPassword( uri.toString(), previousAuthenticationFailed ) )
					return false;
			fillUsernameAndPassword( items );
			return true;
		}

		private void fillUsernameAndPassword( CredentialItem[] items )
		{
			for ( CredentialItem item : items )
				fillItem( item );
		}

		private void fillItem( CredentialItem item )
		{
			if ( item instanceof CredentialItem.Username )
				( ( CredentialItem.Username ) item ).setValue( username );
			else if ( item instanceof CredentialItem.Password )
				( ( CredentialItem.Password ) item ).setValue( password.toCharArray() );
		}
	}
}
