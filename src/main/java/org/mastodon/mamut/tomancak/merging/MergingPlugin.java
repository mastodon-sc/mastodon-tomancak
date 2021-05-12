/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2021 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.merging;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = MamutPlugin.class )
public class MergingPlugin extends AbstractContextual implements MamutPlugin
{
	private static final String MERGE_PROJECTS = "[tomancak] merge projects";

	private static final String[] MERGE_PROJECTS_KEYS = { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( MERGE_PROJECTS, "Merge Projects..." );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( MERGE_PROJECTS, MERGE_PROJECTS_KEYS, "Merge two Mastodon projects." );
		}
	}

	private final AbstractNamedAction mergeProjectsAction;

	private MamutPluginAppModel pluginAppModel;

	public MergingPlugin()
	{
		mergeProjectsAction = new RunnableAction( MERGE_PROJECTS, this::mergeProjects );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( final MamutPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins",
						menu( "Tomancak lab",
								item( MERGE_PROJECTS ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( mergeProjectsAction, MERGE_PROJECTS_KEYS );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		mergeProjectsAction.setEnabled( appModel != null );
	}

	private MergingDialog mergingDialog;

	private void mergeProjects()
	{
		if ( mergingDialog == null )
			mergingDialog = new MergingDialog( null );
		mergingDialog.onMerge( () ->
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
				pluginAppModel.getWindowManager().getProjectManager().open( new MamutProject( null, dsA.project().getDatasetXmlFile() ) );
				final MergeDatasets.OutputDataSet output = new MergeDatasets.OutputDataSet( pluginAppModel.getAppModel().getModel() );
				MergeDatasets.merge( dsA, dsB, output, distCutoff, mahalanobisDistCutoff, ratioThreshold );
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
		} );
		mergingDialog.setVisible( true );
	}
}
