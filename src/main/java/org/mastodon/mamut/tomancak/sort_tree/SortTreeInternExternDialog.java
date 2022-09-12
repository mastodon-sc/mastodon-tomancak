package org.mastodon.mamut.tomancak.sort_tree;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Spot;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Collection;

public class SortTreeInternExternDialog extends JDialog
{
	private static final String description = "<html>"
			+ "<b>Sort the order of the sub lineages in the trackscheme.</b><br>"
			+ "Cells closer to the center landmark, are put to the left side.<br>"
			+ "Cells further away from the center landmark, are put to the right side."
			+ "</html>";

	private final MamutAppModel appModel;

	private final SelectSpotsComponent centerLandmark;
	private final SelectSpotsComponent nodesToSort;

	private SortTreeInternExternDialog( MamutAppModel appModel ) {
		super(( Frame ) null, "Sort Lineage Tree", false);
		setResizable( false );
		this.appModel = appModel;
		centerLandmark = new SelectSpotsComponent( appModel );
		nodesToSort = new SelectSpotsComponent( appModel );
		nodesToSort.addEntireGraphItem();
		nodesToSort.addSelectedNodesItem();
		nodesToSort.setSelectedNodes();
		initGui();
	}

	private void initGui()
	{
		setLayout( new MigLayout("insets dialog","[][]") );
		add(new JLabel(description), "span, wrap");
		add(new JLabel("Center landmark:"));
		add( centerLandmark, "wrap");
		add(new JLabel("Which nodes to sort:"));
		add( nodesToSort, "wrap");
		JButton button = new JButton( "Sort" );
		button.addActionListener( ignore -> sortButtonClicked() );
		add( button, "skip, align right" );
		pack();
		centerWindow( this );
	}

	public static void showDialog(MamutAppModel model)
	{
		SortTreeInternExternDialog dialog = new SortTreeInternExternDialog(model);
		dialog.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		dialog.setVisible( true );
	}

	private void sortButtonClicked()
	{
		Collection<Spot> center = centerLandmark.getSelectedSpots();
		Collection<Spot> selectedSpots = nodesToSort.getSelectedSpots();
		SortTreeInternExtern.sort( appModel.getModel(), selectedSpots, center );
	}

	private static void centerWindow( Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
}
