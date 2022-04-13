package org.mastodon.mamut.tomancak.compact_lineage;

import org.mastodon.mamut.MamutAppModel;

import javax.swing.*;

public class CompactLineageFrame extends JFrame
{

	public CompactLineageFrame(MamutAppModel appModel) {
		super("Compact Lineage");
		add(new CompactLineagePanel(appModel));
		pack();
	}
}
