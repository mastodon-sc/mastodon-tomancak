package org.mastodon.mamut.tomancak.lineage_registration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistrationMethod;

/**
 * Dialog for the {@link LineageRegistrationPlugin}. It allows to select two
 * {@link MamutProject}s and to perform various actions on them.
 */
public class LineageRegistrationFrame extends JFrame
{
	private static final String FIRST_TIMEPOINT_TOOLTIP = "<html><body>"
			+ "The first time point of a project to be used for the registration.<br>"
			+ "<br>"
			+ "This is useful if both projects start at different stages, or with<br>"
			+ "less than three cells in the first timepoint. In this case:<br>"
			+ "Select a stage of the embryo. For example 4 cell stage.<br>"
			+ "And for both projects enter a time point at which the embryo has 4 cells.<br>"
			+ "</body></html>";

	private static final String SORT_TRACKSCHEME_TOOLTIP = "<html><body>"
			+ "Orders the descendants in the TrackScheme of this project<br>"
			+ "such that their order matches the order in the other project."
			+ "</body></html>";

	private static final String TAG_CELLS_TOOLTIP = "<html><body>"
			+ "<b>Creates a new tag set \"lineage registration\" in the selected project.</b><br>"
			+ "<br>"
			+ "The tag set contains two tags:"
			+ "<ul>"
			+ "<li><b>flipped:</b> A cell, that is first child cell in one project, and matched "
			+ "to a second child cell in the other project."
			+ "<li><b>unmatched:</b> A cells that could not be matched.</li>"
			+ "</ul>"
			+ "</body></html>";

	private static final String COPY_TAGSET_TOOLTIP = "<html><body>"
			+ "Use the found correspondences to copy a tag set from one project to the other.<br>"
			+ "<br>"
			+ "The correspondences are on a level of cells / branches.<br>"
			+ "That is why the tags are only copied if the entire cell / branch is tagged.<br>"
			+ "Tags on individual spot are therefor not copied."
			+ "</body></html>";

	private static final String PLOT_ANGLES_TOOLTIP = "<html><body>"
			+ "Show a plot of angles between paired cell division directions over time.<br>"
			+ "</body></html>";

	private static final String ANGLES_FEATURE_TOOLTIP = "<html><body>"
			+ "Stores the angles between paired cell division directions<br>"
			+ "as a feature in both projects.<br>"
			+ "</body></html>";

	private static final String TAG_LINEAGES_TOOLTIP = "<html><body>"
			+ "Creates a new tag set \"lineages\" in both projects<br>"
			+ "Lineages with the same root node label get a tag with the same color.<br>"
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

	private final JFormattedTextField firstTimepointA = createNumberTextField();

	private final JFormattedTextField firstTimepointB = createNumberTextField();

	private final JComboBox< SpatialRegistrationMethod > spatialRegistrationComboBox =
			new JComboBox<>( SpatialRegistrationMethod.values() );

	private final List< JToggleButton > syncGroupButtons;

	private final List< JComponent > enableDisable = new ArrayList<>();

	private final JTextArea logArea;

	public LineageRegistrationFrame( Listener listener )
	{
		super( "Lineage Registration Across Two Mastodon Projects" );
		this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		this.setLocationByPlatform( true );
		this.listener = listener;
		setLayout( new MigLayout( "insets dialog, fill" ) );

		add( introductionTextPane(), "span, grow, wrap, width 0:0:" );
		add( new JLabel( "Select Mastodon projects to match:" ), "span, wrap" );
		add( new JLabel( "project A:" ) );
		comboBoxA.addActionListener( ignore -> updateEnableComponents() );
		add( comboBoxA, "grow, wrap" );
		add( new JLabel( "project B:" ) );
		comboBoxB.addActionListener( ignore -> updateEnableComponents() );
		add( comboBoxB, "grow, wrap" );

		add( new JLabel( "First time point for registration:" ), "gaptop unrelated" );
		add( new JLabel( "project A: " ), "split 4" );
		add( firstTimepointA );
		firstTimepointA.setToolTipText( FIRST_TIMEPOINT_TOOLTIP );
		enableDisable.add( firstTimepointA );
		add( new JLabel( "project B: " ), "gapbefore unrelated" );
		add( firstTimepointB, "wrap" );
		firstTimepointB.setToolTipText( FIRST_TIMEPOINT_TOOLTIP );
		enableDisable.add( firstTimepointB );

		add( new JLabel( "Spatial registration method:" ) );
		spatialRegistrationComboBox.setSelectedItem( SpatialRegistrationMethod.DYNAMIC_ROOTS );
		add( spatialRegistrationComboBox, "wrap" );
		enableDisable.add( spatialRegistrationComboBox );

		add( new JLabel( "Tag unmatched & flipped cells:" ), "gaptop unrelated" );
		add( newOperationButton( "in both projects", TAG_CELLS_TOOLTIP, listener::onTagBothClicked ), "split 3" );
		add( newOperationButton( "project A", TAG_CELLS_TOOLTIP, listener::onTagProjectAClicked ) );
		add( newOperationButton( "project B", TAG_CELLS_TOOLTIP, listener::onTagProjectBClicked ), "wrap" );
		add( new JLabel( "Sort TrackScheme:" ) );
		add( newOperationButton( "project A", SORT_TRACKSCHEME_TOOLTIP, listener::onSortTrackSchemeAClicked ), "split 2" );
		add( newOperationButton( "project B", SORT_TRACKSCHEME_TOOLTIP, listener::onSortTrackSchemeBClicked ), "wrap" );
		add( new JLabel( "Copy tag set:" ) );
		add( newOperationButton( "from A to B ...", COPY_TAGSET_TOOLTIP, listener::onCopyTagSetAtoB ), "split 2" );
		add( newOperationButton( "from B to A ...", COPY_TAGSET_TOOLTIP, listener::onCopyTagSetBtoA ), "wrap" );
		add( new JLabel( "Cell division angles:") );
		add( newOperationButton( "plot angles", PLOT_ANGLES_TOOLTIP, listener::onPlotAnglesClicked ), "split 2" );
		add( newOperationButton( "add angles to table", ANGLES_FEATURE_TOOLTIP, listener::onAddAnglesFeatureClicked ), "wrap" );
		add( new JLabel( "Others:" ), "gaptop unrelated" );
		add( newOperationButton( "color paired lineages", TAG_LINEAGES_TOOLTIP, listener::onColorLineagesClicked ), "wrap" );
		add( new JLabel( "Couple projects:" ), "gaptop unrelated" );
		this.syncGroupButtons = initSyncGroupButtons();
		add( syncGroupButtons.get( 0 ), "split 3" );
		add( syncGroupButtons.get( 1 ) );
		add( syncGroupButtons.get( 2 ), "wrap" );
		logArea = new JTextArea( 3, 50 );
		logArea.setEditable( false );
		add( logArea, "gaptop unrelated, span, grow" );
		add( newSimpleButton( "Close", this::onCloseClicked ), "gaptop unrelated, span, align right" );
		updateEnableComponents();
	}

	public void clearLog()
	{
		logArea.setText( "" );
	}

	public void log( final String format, Object... args )
	{
		String text = logArea.getText();
		if ( !text.isEmpty() )
			text += "\n";
		text += String.format( format, args );
		logArea.setText( text );
	}

	private static JFormattedTextField createNumberTextField()
	{
		NumberFormatter numberFormatter = new NumberFormatter( NumberFormat.getIntegerInstance() );
		numberFormatter.setValueClass( Integer.class );
		numberFormatter.setAllowsInvalid( false );
		numberFormatter.setMinimum( 0 );
		JFormattedTextField textField = new JFormattedTextField( numberFormatter );
		textField.setColumns( 5 );
		textField.setText( "0" );
		textField.setHorizontalAlignment( SwingConstants.RIGHT );
		return textField;
	}

	private void updateEnableComponents()
	{
		SelectedProject projectA = getProjectA();
		SelectedProject projectB = getProjectB();
		final boolean enabled = projectA != null && projectB != null && projectA.getProjectModel() != projectB.getProjectModel();
		for ( JComponent b : enableDisable )
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
		enableDisable.add( button );
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
				+ "<li>There needs to be at least three lineages with cell divisions, "
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
			this.enableDisable.add( e );
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

	public void setMastodonInstances( List< ProjectModel > instances )
	{
		SelectedProject a = getProjectA();
		SelectedProject b = getProjectB();
		comboBoxA.removeAllItems();
		comboBoxB.removeAllItems();
		for ( ProjectModel projectModel : instances )
		{
			MastodonInstance mastodonInstance = new MastodonInstance( projectModel );
			comboBoxA.addItem( mastodonInstance );
			comboBoxB.addItem( mastodonInstance );
		}
		setSelected( comboBoxA, a, 0 );
		boolean sameProject = a != null && b != null && a.getProjectModel() == b.getProjectModel();
		setSelected( comboBoxB, sameProject ? null : b, 1 );
		updateEnableComponents();
	}

	private void setSelected( JComboBox< MastodonInstance > comboBox, SelectedProject selectedProject, int defaultIndex )
	{
		if ( selectedProject != null )
		{
			for ( int i = 0; i < comboBox.getItemCount(); i++ )
				if ( comboBox.getItemAt( i ).projectModel == selectedProject.getProjectModel() )
				{
					comboBox.setSelectedIndex( i );
					return;
				}
		}
		if ( defaultIndex < comboBox.getItemCount() )
			comboBox.setSelectedIndex( defaultIndex );
	}

	public SelectedProject getProjectA()
	{
		return getSelected( comboBoxA, firstTimepointA );
	}

	public SelectedProject getProjectB()
	{
		return getSelected( comboBoxB, firstTimepointB );
	}

	public SpatialRegistrationMethod getSpatialRegistrationMethod()
	{
		return ( SpatialRegistrationMethod ) spatialRegistrationComboBox.getSelectedItem();
	}

	private SelectedProject getSelected( JComboBox< MastodonInstance > comboBoxA, JFormattedTextField firstTimepointTextField )
	{
		Object selectedItem = comboBoxA.getSelectedItem();
		if ( selectedItem == null )
			return null;
		ProjectModel projectModel = ( ( MastodonInstance ) selectedItem ).projectModel;
		Object value = firstTimepointTextField.getValue();
		int firstTimepoint = value == null ? 0 : ( int ) value;
		return new SelectedProject( projectModel, getProjectName( projectModel.getProject() ), firstTimepoint );
	}

	private static class MastodonInstance
	{

		private final ProjectModel projectModel;

		private MastodonInstance( ProjectModel projectModel )
		{
			this.projectModel = projectModel;
		}

		@Override
		public String toString()
		{
			return getProjectName( projectModel.getProject() );
		}
	}

	public static String getProjectName( MamutProject mamutProject )
	{
		try
		{
			return FilenameUtils.getBaseName( mamutProject.getProjectRoot().getName() );
		}
		catch ( NullPointerException e )
		{
			return mamutProject.toString();
		}
	}

	public interface Listener
	{

		void onSortTrackSchemeAClicked();

		void onSortTrackSchemeBClicked();

		void onColorLineagesClicked();

		void onCopyTagSetAtoB();

		void onCopyTagSetBtoA();

		void onTagBothClicked();

		void onTagProjectAClicked();

		void onTagProjectBClicked();

		void onSyncGroupClicked( int i );

		void onPlotAnglesClicked();

		void onAddAnglesFeatureClicked();
	}

	private static class DummyListener implements Listener
	{

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

		@Override
		public void onPlotAnglesClicked()
		{

		}

		@Override
		public void onAddAnglesFeatureClicked()
		{

		}
	}

	public static void main( String... args )
	{
		// NOTE: Small demo function that only shows the LineageRegistrationDialog. For easy debugging.
		LineageRegistrationFrame dialog = new LineageRegistrationFrame( new DummyListener() );
		dialog.pack();
		dialog.setVisible( true );
	}

}
