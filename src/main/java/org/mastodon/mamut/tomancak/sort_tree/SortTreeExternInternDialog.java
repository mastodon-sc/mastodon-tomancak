/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

public class SortTreeExternInternDialog extends JDialog
{
	private static final String description = "<html>"
			+ "<b>Sort the order of the sub lineages in the trackscheme.</b><br>"
			+ "Cells further away from the center landmark, are put to the left side.<br>"
			+ "Cells closer to the center landmark, are put to the right side."
			+ "</html>";

	private final MamutAppModel appModel;

	private final SelectSpotsComponent centerLandmark;
	private final SelectSpotsComponent nodesToSort;

	private SortTreeExternInternDialog( MamutAppModel appModel ) {
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
		SortTreeExternInternDialog dialog = new SortTreeExternInternDialog(model);
		dialog.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		dialog.setVisible( true );
	}

	private void sortButtonClicked()
	{
		Collection<Spot> center = centerLandmark.getSelectedSpots();
		Collection<Spot> selectedSpots = nodesToSort.getSelectedSpots();
		SortTree.sortExternIntern( appModel.getModel(), selectedSpots, center );
	}

	private static void centerWindow( Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}
}
