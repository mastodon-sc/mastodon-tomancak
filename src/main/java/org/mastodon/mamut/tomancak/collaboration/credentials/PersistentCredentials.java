package org.mastodon.mamut.tomancak.collaboration.credentials;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * This class is meant to be used with JGIT for a comfortable way to ask the
 * user for credentials. The user is only asked once for username and password.
 * The credentials are stored in memory and reused for all subsequent requests.
 * It also tries to detect if the user entered wrong credentials,
 * and asks for new credentials.
 */
public class PersistentCredentials
{

	private String username = null;

	private String password = null;

	/**
	 * This method simply returns the username and password if they are already
	 * known. It asks the user for credentials if they are not known yet, or if
	 * the previous attempt to use the credentials failed.
	 */
	private synchronized Pair< String, String > getUsernameAndPassword( URIish uri, boolean authenticationFailure )
	{
		boolean missingCredentials = password == null || username == null;
		if ( missingCredentials || authenticationFailure )
			if ( !queryPassword( uri.toString(), authenticationFailure ) )
				return null;
		return Pair.of( username, password );
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
		panel.add( new JLabel( "password" ) );
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
		return new SingleUseCredentialsProvider( this::getUsernameAndPassword );
	}
}
