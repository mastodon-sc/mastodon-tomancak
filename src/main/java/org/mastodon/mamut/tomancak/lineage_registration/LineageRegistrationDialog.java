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
	
	public LineageRegistrationDialog() {
		super(( JFrame ) null,"Sort TrackScheme to Match Another Lineage", true);
		setLayout( new MigLayout("insets dialog, fill") );

		final String introText = "<html><body>"
				+ "The \"Tree Matching\" plugin allows to sorts the TrackScheme in this project<br>" 
				+ "such that the order matches the other project.<br>"
				+ "These requirements should be met:"
				+ "<ul>"
				+ "<li>Both project should show a stereotypically developing embryo.</li>"
				+ "<li>The first frame should show the two embryo at a similar stage.</li>"
				+ "<li>Root nodes must be named, and the names should match between the two projects.</li>"
				+ "</ul>"
				+ "</body></html>";
		add(new JLabel("Please select Mastodon project to match to:"), "wrap");
		pathTextArea = new JTextArea(2, 50);
		pathTextArea.setEditable( false );
		add(pathTextArea, "grow, wrap");
		add( newButton( "select", this::onSelectClicked ), "wrap");
		add(new JLabel(introText), "gaptop unrelated, wrap");
		add( newButton( "Sort TrackScheme", this::onOkClicked ), "gaptop unrelated, split 2, pushx, align right" );
		add( newButton( "Cancel", this::onCancelClicked ) );
	}

	private void onSelectClicked()
	{
		mastodonProject = FileChooser.chooseFile( this, null, new FileNameExtensionFilter( "Mastodon project", "mastodon" ), "Open Mastodon Project, To Match To", FileChooser.DialogType.LOAD );
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
	
	public static File showDialog() {
		LineageRegistrationDialog dialog = new LineageRegistrationDialog();
		dialog.setLocationByPlatform( true );
		dialog.pack();
		dialog.setVisible( true );
		return dialog.ok ? dialog.mastodonProject : null;
	}
	
	public static void main(String... args) {
		File file = LineageRegistrationDialog.showDialog();
		System.out.println(file);
	}
}
