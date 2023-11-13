package org.mastodon.mamut.tomancak.collaboration;

import java.awt.KeyboardFocusManager;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * A dialog that asks the user to enter a commit message.
 * The dialog can be closed by using the mouse to clock OK or Cancel,
 * by pressing CTRL-ENTER or ESCAPE, or by pressing TAB and ENTER.
 */
public class CommitMessageDialog
{

	/**
	 * Show a dialog that asks the user to enter a commit message.
	 *
	 * @param title The title of the dialog.
	 * @return The commit message as a String, or null if the dialog was cancelled.
	 */
	public static String showDialog( String title )
	{
		JTextArea textArea = new JTextArea( 5, 40 );

		// Make the tab key move focus to the next component and shift-tab to the previous.
		textArea.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null );
		textArea.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null );

		JPanel panel = new JPanel();
		panel.setLayout( new MigLayout( "insets dialog" ) );
		panel.add( new JLabel( "Save point message:" ), "wrap" );
		panel.add( new JScrollPane( textArea ), "wrap" );
		panel.add( new JLabel( "Please describe briefly the changes since the last save point!" ) );

		// Show a JOptionPane, where the TextArea has focus when the dialog is shown.
		JOptionPane optionPane = new JOptionPane( panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION )
		{
			@Override
			public void selectInitialValue()
			{
				super.selectInitialValue();
				textArea.requestFocusInWindow();
			}
		};
		JDialog dialog = optionPane.createDialog( "Add Save Point (commit)" );
		dialog.setVisible( true );
		dialog.dispose();
		Object result = optionPane.getValue();
		if ( result instanceof Integer && ( int ) result == JOptionPane.OK_OPTION )
			return textArea.getText();
		else
			return null;
	}
}
