package org.mastodon.mamut.tomancak;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.IOException;
import mpicbg.spim.data.SpimDataException;


public class StartMastodonOnProject {

	public static void main(String[] args) {
		try {
			String projectPath = fileOpenDialog();

			//not sure what this is good for but see it everywhere...
			//(seems to give no effect on Linux)
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );

			//the central hub, a container to hold all
			final WindowManager windowManager = new WindowManager( new Context() );
			windowManager.getProjectManager().open( new MamutProjectIO().load( projectPath ) );

			//a GUI element wrapping around the hub
			final MainWindow win = new MainWindow(windowManager);

			//this makes the true Mastodon window visible
			//note: you can open project that restores/reopen e.g. TrackScheme window,
			//      yet the main Mastodon window is not shown... but this is runs non-stop
			win.setVisible( true );

			//this makes the whole thing (incl. the central hub) go down when the GUI is closed
			win.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		} catch (IOException | SpimDataException e) {
			e.printStackTrace();
		}
	}

	private static String fileOpenDialog()
	{
		JFileChooser fileChooser = new JFileChooser("Open Mastodon Project");
		fileChooser.setFileFilter( new FileNameExtensionFilter( "Mastodon Project (*.mastodon)", "mastodon" ) );
		fileChooser.showOpenDialog( null );
		return fileChooser.getSelectedFile().getAbsolutePath();
	}
}
