package org.mastodon.mamut.tomancak.resolve;

import java.io.IOException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;
import org.scijava.ui.UIService;

import mpicbg.spim.data.SpimDataException;

public class LocateTagsFrameDemo
{
	public static void main( final String[] args ) throws SpimDataException, IOException
	{
		final Context context = new Context();
		context.service( UIService.class ).showUI();
		final ProjectModel projectModel = ProjectLoader.open( "/home/arzt/Datasets/DeepLineage/Trackathon/try_resolve_conflicts.mastodon", context );
		CreateConflictTagSet.run( projectModel.getModel(), "Conflicting Spots", 0.5 );
		final MainWindow mainWindow = new MainWindow( projectModel );
		mainWindow.setDefaultCloseOperation( MainWindow.DISPOSE_ON_CLOSE );
		mainWindow.setVisible( true );
		final LocateTagsFrame dialog = new LocateTagsFrame( projectModel );
		dialog.setSize( 400, 600 );
		dialog.setVisible( true );
	}
}
