package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import org.mastodon.ui.util.FileChooser;

public class LineageRegistrationDialog extends JDialog
{
	private final JTextArea pathTextArea;

	private File mastodonProject = null;

	private boolean ok = false;

	public LineageRegistrationDialog()
	{
		// NB: Setting the ModalityType.DOCUMENT_MODEL has the following intended effect:
		//     1. setVisible(true) block, until the window ok / cancel / close is clicked.
		//     2. Other Fiji windows are not blocked.
		super( ( JFrame ) null, "Sort TrackScheme to Match Another Lineage", ModalityType.DOCUMENT_MODAL );
		setLayout( new MigLayout( "insets dialog, fill" ) );

		final String introText = "<html><body>"
				+ "The \"Tree Matching\" plugin orders the descendants in the TrackScheme of this project<br>"
				+ "such that their order matches the order in the other project.<br><br>"
				+ "These requirements should be met:"
				+ "<ul>"
				+ "<li>Both projects should show a stereotypically developing embryos.</li>"
				+ "<li>The first frames should show the two embryos at a similar stage.</li>"
				+ "<li>Root nodes must be named, and the names should match between the two projects.</li>"
				+ "</ul>"
				+ "</body></html>";
		add( new JLabel( "Please select Mastodon project to match to:" ), "wrap" );
		pathTextArea = new JTextArea( 2, 50 );
		pathTextArea.setLineWrap( true );
		pathTextArea.setEditable( false );
		add( pathTextArea, "grow, wmin 0, wrap" );
		add( newButton( "select", this::onSelectClicked ), "wrap" );
		add( new JLabel( introText ), "gaptop unrelated, wrap" );
		add( newButton( "Sort TrackScheme", this::onOkClicked ), "gaptop unrelated, split 2, pushx, align right" );
		add( newButton( "Cancel", this::onCancelClicked ) );
	}

	private void onSelectClicked()
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter( "Mastodon project", "mastodon" );
		String title = "Open Mastodon project to match to";
		mastodonProject = FileChooser.chooseFile( this, null, filter, title,
				FileChooser.DialogType.LOAD, FileChooser.SelectionMode.FILES_AND_DIRECTORIES );
		if ( mastodonProject == null )
			return;
		pathTextArea.setText( mastodonProject.getPath() );
	}

	private void onOkClicked()
	{
		ok = true;
		super.setVisible( false );
	}

	private void onCancelClicked()
	{
		super.setVisible( false );
	}

	private JButton newButton( String select, Runnable action )
	{
		JButton button = new JButton( select );
		button.addActionListener( ignored -> action.run() );
		return button;
	}

	public static File showDialog()
	{
		LineageRegistrationDialog dialog = new LineageRegistrationDialog();
		dialog.setLocationByPlatform( true );
		dialog.pack();
		dialog.setVisible( true );
		return dialog.ok ? dialog.mastodonProject : null;
	}

	public static void main( String... args )
	{
		File file = LineageRegistrationDialog.showDialog();
		System.out.println( file );
	}
}
