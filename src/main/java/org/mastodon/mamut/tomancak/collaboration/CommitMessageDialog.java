package org.mastodon.mamut.tomancak.collaboration;

import java.awt.KeyboardFocusManager;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

public class CommitMessageDialog
{
	public static void main( String... args )
	{
		System.out.println( showDialog( "Add Save Point (Commit)" ) );
	}

	public static String showDialog( String title )
	{
		JTextArea textArea = new JTextArea( 5, 40 );
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
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		dialog.setVisible( true );
		Object result = optionPane.getValue();
		if ( result instanceof Integer && ( int ) result == JOptionPane.OK_OPTION )
			return textArea.getText();
		else
			return null;
	}
}
