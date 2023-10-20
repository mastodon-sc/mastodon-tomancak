package org.mastodon.mamut.tomancak.collaboration;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class NotificationDialog
{
	public static void main( String... args )
	{
		show( "Upload Changes", "<html><body><font size=+4 color=green>&#10003</font> Changes were uploaded successfully." );
	}

	public static void show( String title, String message )
	{
		JOptionPane pane = new JOptionPane( message, JOptionPane.PLAIN_MESSAGE );
		JDialog dialog = pane.createDialog( null, title );
		dialog.setModal( false );
		dialog.setVisible( true );
		new Timer( 1500, ignore -> dialog.setVisible( false ) ).start();
	}
}
