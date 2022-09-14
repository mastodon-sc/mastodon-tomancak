package org.mastodon.mamut.tomancak.sort_tree;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import mpicbg.spim.data.SpimDataException;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import java.io.IOException;

public class SelectSpotsComponentDemo
{
	public static void main(String... args)
			throws IOException, SpimDataException
	{
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( new MamutProjectIO().load( "/home/arzt/Datasets/Mette/E1.mastodon" ) );
		MamutAppModel appModel = windowManager.getAppModel();
		JFrame frame = new JFrame("SelectSpotsComponent Demo");
		frame.setLayout( new MigLayout() );
		frame.add(new JLabel("Select:"), "wrap");
		frame.add(new SelectSpotsComponent( appModel ));
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
}
