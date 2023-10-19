package org.mastodon.mamut.tomancak.collaboration;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ErrorDialog
{
	public static void showErrorMessage( String title, Exception e )
	{
		e.printStackTrace();
		// show the error message in a nice dialog
		String message = "\nThere was a problem:\n\n" + e.getMessage() + "\n";
		SwingUtilities.invokeLater( () -> JOptionPane.showMessageDialog( null, message, title + " (Error)", JOptionPane.ERROR_MESSAGE ) );
	}

	public static void main( String... args )
	{
		showErrorMessage( "Test", new Exception( "Test exception" ) );
	}
}
