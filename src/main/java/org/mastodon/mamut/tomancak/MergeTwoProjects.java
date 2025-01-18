/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

@Plugin( type = Command.class, menuPath = "Plugins > Tracking > Mastodon > Merge Two Projects" )
public class MergeTwoProjects implements Command
{
	@Parameter
	private Context context;

	private MergingDialog mergingDialog;

	@Override
	public void run()
	{
		if ( mergingDialog == null )
		{
			mergingDialog = new MergingDialog( null );
			mergingDialog.onMerge( this::mergeProjects );
		}
		mergingDialog.setVisible( true );
	}

	private void mergeProjects()
	{
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
	}
}
