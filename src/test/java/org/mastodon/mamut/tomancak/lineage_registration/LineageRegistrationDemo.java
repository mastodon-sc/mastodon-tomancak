package org.mastodon.mamut.tomancak.lineage_registration;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.feature.FeatureSpecsService;
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

	private static void openAppModel( Context context, String projectPath )
	{
		try
		{
			ProjectModel projectModel = ProjectLoader.open( projectPath, context );
			projectModel.getWindowManager().createView( MamutViewTrackScheme.class );
			new MainWindow( projectModel ).setVisible( true );
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
