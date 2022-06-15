package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.File;
import java.io.IOException;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;

/**
 * Shows the {@link LineageRegistrationAlgorithm} and
 * executes the {@link LineageRegistrationAlgorithm} when
 * ok is clicked.
 */
public class LineageRegistrationPlugin
{
	public static void showDialog( MamutAppModel appModel )
	{
		File otherProject = LineageRegistrationDialog.showDialog();
		if( otherProject == null)
			return;
		Model model = openModel( otherProject );
		LineageRegistrationAlgorithm.run( model, appModel.getModel() );
	}
	
	private static Model openModel( File file ) {
		try
		{
			MamutProject project = new MamutProjectIO().load( file.getAbsolutePath() );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				model.loadRaw( reader );
			}
			return model;
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}
}
