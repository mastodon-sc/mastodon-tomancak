package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;

public class LineageRegistrationDialog extends JDialog
{
	private static final String SORT_TRACKSCHEME_TOOLTIP = "<html><body>"
			+ "Orders the descendants in the TrackScheme of this project<br>"
			+ "such that their order matches the order in the other project."
			+ "</body></html>";

	private static final String TAG_CELLS_TOOLTIP = "<html><body>"
			+ "Creates a new tag set \"lineage registration\" in the selected project.<br>"
			+ "The tag set contains two tags \"flipped\" and \"unmatched\".<br>"
			+ "Cells that could not be matched are tagged with \"unmatched\".<br>"
			+ "Cells for which the order of the descendants is opposite in both projects are tagged with \"flipped\"."
			+ "</body></html>";

	private static final String COPY_TAGSET_TOOLTIP = "<html><body>"
			+ "Use the found correspondences to copy a tag set from one project to the other.<br>"
			+ "<br>"
			+ "The correspondences are on a level of cells / branches.<br>"
			+ "That is why the tags are only copied if the entire cell / branch is tagged.<br>"
			+ "Tags on individual spot are therefor not copied."
			+ "</body></html>";

	private static final String TAG_LINEAGES_TOOLTIP = "<html><body>"
			+ "Creates a new tag set \"lineages\" in both projects<br>"
			+ "that assigns the same color to paired lineages.<br>"
			+ "(Note: This functionality does not make use of the found correspondences.)"
			+ "</body></html>";

	private static final String COUPLE_PROJECTS_TOOLTIP = "<html><body>"
			+ "Use the found correspondences to couple two projects.<br>"
			+ "<br>"
			+ "Spot highlighting and focus are synchronized between the two projects.<br>"
			+ "Navigation to a clicked spot is synchronized for the selected \"sync group\".<br>"
			+ "Synchronization works best between the \"TrackScheme Branch\" and \"BranchScheme Hierarchy\" windows,<br>"
			+ "(Note: synchronization of edges is not implemented yet.)"
			+ "</body></html>";

	private static final ImageIcon LOCK_ICON = new ImageIcon( GroupLocksPanel.class.getResource( "lock.png" ) );

	private final Listener listener;

	private final JComboBox< MastodonInstance > comboBoxA = new JComboBox<>();

	private final JComboBox< MastodonInstance > comboBoxB = new JComboBox<>();

	private final List< JToggleButton > syncGroupButtons;

	private final List< JComponent > buttons = new ArrayList<>();

	public LineageRegistrationDialog( Listener listener )
	{
		super( ( JFrame ) null, "Lineage Registration Across Two Mastodon Projects", false );
		this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		this.setLocationByPlatform( true );
		this.listener = listener;
		setLayout( new MigLayout( "insets dialog, fill" ) );

		add( introductionTextPane(), "span, grow, wrap, width 0:0:" );
		add( new JLabel( "Select Mastodon projects to match:" ), "span, wrap" );
		add( new JLabel( "project A:" ) );
		comboBoxA.addActionListener( ignore -> updateEnableButtons() );
		add( comboBoxA, "grow, wrap" );
		add( new JLabel( "project B:" ) );
		comboBoxB.addActionListener( ignore -> updateEnableButtons() );
		add( comboBoxB, "grow, wrap" );
		add( newSimpleButton( "update list of projects", listener::onUpdateClicked ), "skip, wrap" );

		add( new JLabel( "Sort TrackScheme:" ), "gaptop unrelated" );
		add( newOperationButton( "project A", SORT_TRACKSCHEME_TOOLTIP, listener::onSortTrackSchemeAClicked ), "split 2" );
		add( newOperationButton( "project B", SORT_TRACKSCHEME_TOOLTIP, listener::onSortTrackSchemeBClicked ), "wrap" );
		add( new JLabel( "Copy tag set:" ) );
		add( newOperationButton( "from A to B ...", COPY_TAGSET_TOOLTIP, listener::onCopyTagSetAtoB ), "split 2" );
		add( newOperationButton( "from B to A ...", COPY_TAGSET_TOOLTIP, listener::onCopyTagSetBtoA ), "wrap" );
		add( new JLabel( "Tag unmatched & flipped cells:" ) );
		add( newOperationButton( "in both projects", TAG_CELLS_TOOLTIP, listener::onTagBothClicked ), "split 3" );
		add( newOperationButton( "project A", TAG_CELLS_TOOLTIP, listener::onTagProjectAClicked ) );
		add( newOperationButton( "project B", TAG_CELLS_TOOLTIP, listener::onTagProjectBClicked ), "wrap" );
		add( new JLabel( "Others:" ) );
		add( newOperationButton( "color paired lineages", TAG_LINEAGES_TOOLTIP, listener::onColorLineagesClicked ), "wrap" );
		add( new JLabel( "Couple projects:" ) );
		this.syncGroupButtons = initSyncGroupButtons();
		add( syncGroupButtons.get( 0 ), "split 3" );
		add( syncGroupButtons.get( 1 ) );
		add( syncGroupButtons.get( 2 ), "wrap" );
		add( newSimpleButton( "Close", this::onCloseClicked ), "gaptop unrelated, span, align right" );
		updateEnableButtons();
	}

	private void updateEnableButtons()
	{
		WindowManager projectA = getProjectA();
		WindowManager projectB = getProjectB();
		final boolean enabled = projectA != null && projectB != null && projectA != projectB;
		for ( JComponent b : buttons )
			b.setEnabled( enabled );
	}

	private JButton newSimpleButton( String title, Runnable action )
	{
		JButton button = new JButton( title );
		button.addActionListener( ignored -> action.run() );
		return button;
	}

	private JButton newOperationButton( String title, String hint, Runnable action )
	{
		JButton button = new JButton( title );
		button.addActionListener( ignored -> action.run() );
		button.setToolTipText( hint );
		buttons.add( button );
		return button;
	}

	private static JTextPane introductionTextPane()
	{
		final String introText = "<html><body>"
				+ "The \"lineage registration\" plugin allows comparing the lineages of two "
				+ "similarly developing embryos in two Mastodon projects. By analyzing the "
				+ "spindle directions it finds the corresponding cells in both embryos."
				+ "<br><br>"
				+ "The plugin allows performing various operations based on the correspondence "
				+ "information."
				+ "<br><br>"
				+ "The following conditions need to be met for the algorithm to work:"
				+ "<ul>"
				+ "<li>Both projects should show stereotypically developing embryos.</li>"
				+ "<li>The first frames should show both the embryos at a similar developmental stage.</li>"
				+ "<li>Root nodes must be labeled, and the labels should match between the two projects.</li>"
				+ "<li>There needs to be at least three lineages with cell divisions,"
				+ "that can be paired based on their names.</li>"
				+ "</ul>"
				+ "(Note: The plugin ignores lineages that have no cell divisions.)<br><br>"
				+ "</body></html>";
		JTextPane comp = new JTextPane();
		comp.setContentType( "text/html" );
		comp.setText( introText );
		comp.setEditable( false );
		return comp;
	}

	private List< JToggleButton > initSyncGroupButtons()
	{
		ArrayList< JToggleButton > buttons = new ArrayList<>();
		for ( int i = 0; i < 3; i++ )
		{
			JToggleButton e = initToggleButton( i );
			buttons.add( e );
			this.buttons.add( e );
		}
		return buttons;
	}

	private JToggleButton initToggleButton( int i )
	{
		JToggleButton button = new JToggleButton();
		button.setIcon( LOCK_ICON );
		button.setText( Integer.toString( i + 1 ) );
		button.addActionListener( ignore -> onSyncGroupButtonClicked( i ) );
		button.setToolTipText( COUPLE_PROJECTS_TOOLTIP );
		return button;
	}

	private void onSyncGroupButtonClicked( int i )
	{
		for ( int j = 0; j < syncGroupButtons.size(); j++ )
			if ( i != j )
				syncGroupButtons.get( j ).setSelected( false );
		boolean isSelected = syncGroupButtons.get( i ).isSelected();
		listener.onSyncGroupClicked( isSelected ? i : -1 );
	}

	private void onCloseClicked()
	{
		dispose();
	}

	public void setMastodonInstances( List< WindowManager > instances )
	{
		WindowManager a = getProjectA();
		WindowManager b = getProjectB();
		comboBoxA.removeAllItems();
		comboBoxB.removeAllItems();
		for ( WindowManager windowManager : instances )
		{
			MastodonInstance mastodonInstance = new MastodonInstance( windowManager );
			comboBoxA.addItem( mastodonInstance );
			comboBoxB.addItem( mastodonInstance );
		}
		setSelected( comboBoxA, a, 0 );
		setSelected( comboBoxB, b, 1 );
		updateEnableButtons();
	}

	private void setSelected( JComboBox< MastodonInstance > comboBox, WindowManager windowManager, int defaultIndex )
	{
		for ( int i = 0; i < comboBox.getItemCount(); i++ )
			if ( comboBox.getItemAt( i ).windowManager == windowManager )
			{
				comboBox.setSelectedIndex( i );
				return;
			}
		if ( defaultIndex < comboBox.getItemCount() )
			comboBox.setSelectedIndex( defaultIndex );
	}

	public WindowManager getProjectA()
	{
		return getSelected( comboBoxA );
	}

	public WindowManager getProjectB()
	{
		return getSelected( comboBoxB );
	}

	private WindowManager getSelected( JComboBox< MastodonInstance > comboBoxA )
	{
		Object selectedItem = comboBoxA.getSelectedItem();
		if ( selectedItem == null )
			return null;
		return ( ( MastodonInstance ) selectedItem ).windowManager;
	}

	private static class MastodonInstance
	{
		private final WindowManager windowManager;

		private MastodonInstance( WindowManager windowManager )
		{
			this.windowManager = windowManager;
		}

		@Override
		public String toString()
		{
			return getProjectName( windowManager );
		}
	}

	public static String getProjectName( WindowManager windowManager )
	{
		MamutProject project = windowManager.getProjectManager().getProject();
		if ( project == null )
			return windowManager.toString();
		File projectRoot = project.getProjectRoot();
		return FilenameUtils.getBaseName( projectRoot.getName() );
	}

	public interface Listener
	{

		void onUpdateClicked();

		void onSortTrackSchemeAClicked();

		void onSortTrackSchemeBClicked();

		void onColorLineagesClicked();

		void onCopyTagSetAtoB();

		void onCopyTagSetBtoA();

		void onTagBothClicked();

		void onTagProjectAClicked();

		void onTagProjectBClicked();

		void onSyncGroupClicked( int i );
	}

	private static class DummyListener implements Listener
	{

		@Override
		public void onUpdateClicked()
		{

		}

		@Override
		public void onSortTrackSchemeAClicked()
		{

		}

		@Override
		public void onSortTrackSchemeBClicked()
		{

		}

		@Override
		public void onColorLineagesClicked()
		{

		}

		@Override
		public void onCopyTagSetAtoB()
		{

		}

		@Override
		public void onCopyTagSetBtoA()
		{

		}

		@Override
		public void onTagBothClicked()
		{

		}

		@Override
		public void onTagProjectAClicked()
		{

		}

		@Override
		public void onTagProjectBClicked()
		{

		}

		@Override
		public void onSyncGroupClicked( int i )
		{

		}
	}

	public static void main( String... args )
	{
		// NOTE: Small demo function that only shows the LineageRegistrationDialog. For easy debugging.
		LineageRegistrationDialog dialog = new LineageRegistrationDialog( new DummyListener() );
		dialog.pack();
		dialog.setVisible( true );
	}

}
