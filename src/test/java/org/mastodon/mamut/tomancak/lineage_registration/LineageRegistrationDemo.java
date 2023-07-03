package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.scijava.Context;

public class LineageRegistrationDemo
{
	public static final String project1 = "/home/arzt/Datasets/Mette/E1.mastodon";
	public static final String project2 = "/home/arzt/Datasets/Mette/E2.mastodon";

	public static final String Ml_2020_07_23_MIRRORED = "/home/arzt/Datasets/DeepLineage/Johannes/2020-07-23_Ml_NL20-H2B_4-cells_Vlado_mirrored.mastodon";

	public static final String Ml_2020_08_03 = "/home/arzt/Datasets/DeepLineage/Johannes/2020-08-03_Ml_DCV16bit_Subbg_2022-06-17_4-cells_Vlado.mastodon";

	public static final String Ml_2022_01_27 = "/home/arzt/Datasets/DeepLineage/Johannes/2022-01-27_Ml_NL45xNL26_fused_part5_4-cells_Vlado.mastodon";

	public static final String Ml_2022_05_03 = "/home/arzt/Datasets/DeepLineage/Johannes/2022-05-03_Ml_NL46xNL22_4-cells_Vlado.mastodon";

	// 2020-08-03 vs. 2022-05-03 -- 0 mistakes, until 16 cell stage
	// 2020-08-03 vs. 2022-01-27 -- 1 mistake
	// 2022-01-27 vs. 2022-05-03 -- 1 mistake
	// 2020-08-03 vs. 2020-07-23 (mirrored) -- 9 mistakes (2020-08-03 significantly rotates between 4 and 8 cell stage in the video)
	// 2022-01-27 vs. 2020-07-23 (mirrored) -- 0 mistakes (visually confirmed in blender very similar at early stages)
	// 2022-05-03 vs. 2020-07-23 (mirrored) -- 0 mistakes (visually similar)

	// mirroring problem:
	// 2020-08-03 vs. 2020-07-23 -- 12 mistakes
	// 2022-01-27 vs. 2020-07-23 -- 5 mistakes
	// 2022-05-03 vs. 2020-07-23 -- 12 mistakes

	public static void main( String... args )
	{
		Context context = new Context();
		openAppModel( context, project1 );
		openAppModel( context, project2 );
		context.service( LineageRegistrationControlService.class ).showDialog();
	}


	public static WindowManager openAppModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project, false, true );
			//TrackSchemeFrame frame = wm.createBranchTrackScheme().getFrame();
			//String baseName = FilenameUtils.getBaseName( projectPath );
			//frame.setTitle( frame.getTitle() + " " + baseName );
			//MainWindow mainWindow = new MainWindow( wm );
			//mainWindow.setVisible( true );
			//mainWindow.setTitle( mainWindow.getTitle() + " " + baseName );
			return wm;
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
