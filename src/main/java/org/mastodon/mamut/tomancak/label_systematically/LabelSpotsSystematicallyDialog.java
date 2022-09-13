package org.mastodon.mamut.tomancak.label_systematically;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.SelectSpotsComponent;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

public class LabelSpotsSystematicallyDialog extends JDialog
{

	private static final String description = "<html>"
			+ "Derive the name of child cells from the name of the parent,<br>"
			+ "by appending a \"1\" or a \"2\" to the parent cell name.<br>"
			+ "The child cell closer to the center landmark get \"1\" appended.<br>"
			+ "The child cell further away from the center landmark gets \"2\" appended."
			+ "</html>";

	private final MamutAppModel appModel;

	private final SelectSpotsComponent centerLandmark;

	private final JCheckBox renameUnnamedCheckbox;

	private final JCheckBox endsWith1or2Checkbox;

	private final JButton actionButton;

	private LabelSpotsSystematicallyDialog( MamutAppModel appModel ) {
		super(( Frame ) null, "Sort Lineage Tree", false);
		setResizable( false );
		this.appModel = appModel;
		this.centerLandmark = new SelectSpotsComponent( appModel );
		this.renameUnnamedCheckbox = initializeCheckbox( "Cells that don't have a name. (The name is a number)" );
		this.endsWith1or2Checkbox = initializeCheckbox( "Cells whose name ends with \"1\" or \"2\"." );
		this.actionButton = new JButton( "Rename" );
		this.actionButton.addActionListener( ignore -> renameButtonClicked() );
		setActionButtonEnabled();
		initGui();
	}

	private void setActionButtonEnabled()
	{
		actionButton.setEnabled( renameUnnamedCheckbox.isSelected()
				|| endsWith1or2Checkbox.isSelected() );
	}

	private JCheckBox initializeCheckbox( String text )
	{
		JCheckBox renameUnnamedCheckbox = new JCheckBox( text );
		renameUnnamedCheckbox.addActionListener( ignore -> setActionButtonEnabled() );
		return renameUnnamedCheckbox;
	}

	private void initGui()
	{
		setLayout( new MigLayout("insets dialog","[]") );
		add(new JLabel(description), "wrap");
		add(new JLabel("Center landmark:"), "split 2");
		add( centerLandmark, "grow, wrap");
		add(new JLabel("Which cells to rename:"), "wrap");
		add( renameUnnamedCheckbox, "wrap" );
		add( endsWith1or2Checkbox, "wrap" );
		add( actionButton, "split 2, align right" );
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( ignore -> dispose() );
		add( cancelButton, "align right" );
		pack();
		centerWindow( this );
	}

	public static void showDialog(MamutAppModel model)
	{
		LabelSpotsSystematicallyDialog dialog = new LabelSpotsSystematicallyDialog(model);
		dialog.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		dialog.setVisible( true );
	}

	private void renameButtonClicked()
	{
		Collection<Spot> center = centerLandmark.getSelectedSpots();
		Model model = appModel.getModel();
		ModelGraph graph = model.getGraph();
		boolean renameUnnamed = renameUnnamedCheckbox.isSelected();
		boolean renameLabelsEndingWith1Or2 = endsWith1or2Checkbox.isSelected();
		Lock writeLock = model.getGraph().getLock().writeLock();
		try
		{
			LabelSpotsSystematically.setLabelsBasedOnInternExtern( graph, center, renameUnnamed, renameLabelsEndingWith1Or2 );
			model.setUndoPoint();
		}
		finally
		{
			writeLock.unlock();
		}
		dispose();
	}

	private static void centerWindow( Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
}
