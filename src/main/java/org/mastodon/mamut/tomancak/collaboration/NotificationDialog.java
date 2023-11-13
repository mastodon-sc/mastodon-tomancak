package org.mastodon.mamut.tomancak.collaboration;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * A dialog that shows a message for a short time and then disappears.
 */
public class NotificationDialog
{

	public static void show( String title, String message )
	{
		JOptionPane pane = new JOptionPane( message, JOptionPane.PLAIN_MESSAGE );
		JDialog dialog = pane.createDialog( null, title );
		dialog.setModal( false );
		dialog.setVisible( true );
		new Timer( 1500, ignore -> dialog.dispose() ).start();
	}
}
