package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

public class LineageRegistrationDemo
{

	public static void main( String... args )
	{
		Context context = new Context();
		WindowManager projectA = openAppModel( context, "/home/arzt/Datasets/Mette/E1.mastodon" );
		WindowManager projectB = openAppModel( context, "/home/arzt/Datasets/Mette/E2.mastodon" );
		projectA.createTrackScheme();
		projectB.createTrackScheme();
		context.service( LineageRegistrationControlService.class ).showDialog();
	}


	private static WindowManager openAppModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			new MainWindow( wm ).setVisible( true );
			return wm;
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
