package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.views.trackscheme.MamutViewTrackScheme;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.scijava.Context;

public class LineageRegistrationDemo
{
	public static final String project1 = "/home/arzt/Datasets/Mette/E1.mastodon";
	public static final String project2 = "/home/arzt/Datasets/Mette/E2.mastodon";

	public static void main( String... args )
	{
		Context context = new Context();
		openAppModel( context, project1 );
		openAppModel( context, project2 );
		context.service( LineageRegistrationControlService.class ).showDialog();
	}


	private static WindowManager openAppModel( Context context, String projectPath )
	{
		try
		{
			ProjectModel projectModel = ProjectLoader.open( projectPath, context );
			WindowManager wm = projectModel.getWindowManager();
			TrackSchemeFrame frame = wm.createView( MamutViewTrackScheme.class ).getFrame();
			String baseName = FilenameUtils.getBaseName( projectPath );
			frame.setTitle( frame.getTitle() + " " + baseName );
			MainWindow mainWindow = new MainWindow( projectModel );
			mainWindow.setVisible( true );
			mainWindow.setTitle( mainWindow.getTitle() + " " + baseName );
			return wm;
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
