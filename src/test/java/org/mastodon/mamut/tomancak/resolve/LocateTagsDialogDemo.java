package org.mastodon.mamut.tomancak.resolve;

import java.io.IOException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class LocateTagsDialogDemo
{
	public static void main( final String[] args ) throws SpimDataException, IOException
	{
		try (final Context context = new Context())
		{
			final ProjectModel projectModel = ProjectLoader.open( "/home/arzt/Datasets/DeepLineage/Trackathon/try_resolve_conflicts.mastodon", context );
			DetectOverlappingSpots.run( projectModel.getModel() );
			MainWindow mainWindow = new MainWindow( projectModel );
			mainWindow.setDefaultCloseOperation( MainWindow.DISPOSE_ON_CLOSE );
			mainWindow.setVisible( true );
			final LocateTagsDialog dialog = new LocateTagsDialog( projectModel );
			dialog.setSize( 400, 600 );
			dialog.setVisible( true );
		}
	}
}
