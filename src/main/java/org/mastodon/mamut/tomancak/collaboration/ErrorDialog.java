package org.mastodon.mamut.tomancak.collaboration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * A dialog that shows an exception message and stack trace.
 * The stack trace is hidden by default and can be shown by
 * clicking on "details".
 */
public class ErrorDialog
{

	public static void showErrorMessage( String title, Exception exception )
	{
		SwingUtilities.invokeLater( () -> showDialog( null, title + " (Error)", exception ) );
	}

	private static void showDialog( Frame parent, String title, Exception exception )
	{
		String message = "\nThere was a problem:\n\n  " + exception.getMessage() + "\n\n";
		final JScrollPane scrollPane = initScrollPane( exception );
		final JCheckBox checkBox = new JCheckBox( "show details" );
		checkBox.setForeground( Color.BLUE );
		Object[] objects = { message, checkBox, scrollPane };
		JOptionPane pane = new JOptionPane( objects, JOptionPane.ERROR_MESSAGE );
		JDialog dialog = pane.createDialog( parent, title );
		dialog.setResizable( true );
		checkBox.addItemListener( event -> {
			boolean visible = event.getStateChange() == ItemEvent.SELECTED;
			scrollPane.setVisible( visible );
			scrollPane.setPreferredSize( visible ? null : new Dimension( 0, 0 ) );
			dialog.pack();
		} );
		dialog.setModal( true );
		dialog.pack();
		dialog.setVisible( true );
		dialog.dispose();
	}

	private static JScrollPane initScrollPane( Exception exception )
	{
		String stackTrace = ExceptionUtils.getStackTrace( exception );
		int lines = Math.min( 20, countLines( stackTrace ) );
		JTextArea textArea = new JTextArea( stackTrace, lines, 70 );
		textArea.setForeground( new Color( 0x880000 ) );
		textArea.setEditable( false );
		textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize() ) );
		final JScrollPane scrollPane = new JScrollPane( textArea );
		scrollPane.setVisible( false );
		return scrollPane;
	}

	private static int countLines( String str )
	{
		String[] lines = str.split( "\r\n|\r|\n" );
		return lines.length;
	}
}
