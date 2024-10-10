package org.mastodon.mamut.tomancak;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectCreator;
import org.mastodon.mamut.tomancak.merging.Dataset;
import org.mastodon.mamut.tomancak.merging.MergeDatasets;
import org.mastodon.mamut.tomancak.merging.MergingDialog;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, label = "Mastodon - Merge two mastodon projects into one project", menuPath = "Plugins > Tracking > Mastodon > Merge Two Projects" )
public class MergeTwoProjects implements Command
{
	@Parameter
	private Context context;

	@Override
	public void run()
	{
		mergeProjects();
	}

	private MergingDialog mergingDialog;

	private void mergeProjects()
	{
		if ( mergingDialog == null )
			mergingDialog = new MergingDialog( null );
		mergingDialog.onMerge( () -> {
			try
			{
				final String pathA = mergingDialog.getPathA();
				final String pathB = mergingDialog.getPathB();
				final double distCutoff = mergingDialog.getDistCutoff();
				final double mahalanobisDistCutoff = mergingDialog.getMahalanobisDistCutoff();
				final double ratioThreshold = mergingDialog.getRatioThreshold();

				final Dataset dsA = new Dataset( pathA );
				final Dataset dsB = new Dataset( pathB );

				final ProjectModel projectMerged = ProjectCreator.createProjectFromBdvFile( dsA.project().getDatasetXmlFile(), context );
				final MergeDatasets.OutputDataSet output = new MergeDatasets.OutputDataSet( projectMerged.getModel() );
				MergeDatasets.merge( dsA, dsB, output, distCutoff, mahalanobisDistCutoff, ratioThreshold );
				// start a new instance of Mastodon that shows the result of the merge operation
				new MainWindow( projectMerged ).setVisible( true );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} );
		mergingDialog.setVisible( true );
	}
}
