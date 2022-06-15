package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

public class LineageRegistrationDemo
{

	public Model embryoA;

	public Model embryoB;

	public static void main( String... args )
	{
		new LineageRegistrationDemo().run();
	}

	private LineageRegistrationDemo()
	{
		Context context = new Context();
		embryoA = openAppModel( context, "/home/arzt/Datasets/Mette/E1.mastodon" ).getModel();
		embryoB = openAppModel( context, "/home/arzt/Datasets/Mette/E2.mastodon" ).getModel();
	}

	public void run()
	{
		LineageColoring.tagLineages( embryoA, embryoB );
		LineageRegistrationAlgorithm.run( embryoA, embryoB );
	}

	private static MamutAppModel openAppModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			new MainWindow( wm ).setVisible( true );
			return wm.getAppModel();
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
