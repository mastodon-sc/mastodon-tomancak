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
package org.mastodon.mamut.tomancak;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.tomancak.compact_lineage.CompactLineageFrame;
import org.mastodon.mamut.tomancak.export.MakePhyloXml;
import org.mastodon.mamut.tomancak.merging.Dataset;
import org.mastodon.mamut.tomancak.merging.MergeDatasets;
import org.mastodon.mamut.tomancak.merging.MergingDialog;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeDialog;
import org.mastodon.mamut.tomancak.spots.InterpolateMissingSpots;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = MamutPlugin.class )
public class TomancakPlugins extends AbstractContextual implements MamutPlugin
{
	private static final String EXPORT_PHYLOXML = "[exports] export phyloxml for selection";
	private static final String FLIP_DESCENDANTS = "[trees] flip descendants";
	private static final String COPY_TAG = "copy tag";
	private static final String INTERPOLATE_SPOTS = "[trees] interpolate missing spots";
	private static final String LABEL_SELECTED_SPOTS = "[trees] label selected spots";
	private static final String COMPACT_LINEAGE_VIEW = "[displays] show compact lineage";
	private static final String SORT_TREE = "[trees] sort lineage tree";
	private static final String MERGE_PROJECTS = "merge projects";
	private static final String TWEAK_DATASET_PATH = "fix project image path";

	private static final String[] EXPORT_PHYLOXML_KEYS = { "not mapped" };
	private static final String[] FLIP_DESCENDANTS_KEYS = { "not mapped" };
	private static final String[] COPY_TAG_KEYS = { "not mapped" };
	private static final String[] INTERPOLATE_SPOTS_KEYS = { "not mapped" };
	private static final String[] LABEL_SELECTED_SPOTS_KEYS = { "not mapped" };
	private static final String[] COMPACT_LINEAGE_VIEW_KEYS = { "not mapped" };
	private static final String[] SORT_TREE_KEYS = { "not mapped" };
	private static final String[] MERGE_PROJECTS_KEYS = { "not mapped" };
	private static final String[] TWEAK_DATASET_PATH_KEYS = { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( EXPORT_PHYLOXML, "Export phyloXML for Selection" );
		menuTexts.put( FLIP_DESCENDANTS, "Flip Descendants" );
		menuTexts.put( COPY_TAG, "Copy Tag" );
		menuTexts.put( INTERPOLATE_SPOTS, "Interpolate Missing Spots" );
		menuTexts.put( LABEL_SELECTED_SPOTS, "Label Selected Spots" );
		menuTexts.put( COMPACT_LINEAGE_VIEW, "Show Compact Lineage" );
		menuTexts.put( SORT_TREE, "Sort Lineage Tree" );
		menuTexts.put( MERGE_PROJECTS, "Merge Two Projects" );
		menuTexts.put( TWEAK_DATASET_PATH, "Fix Image Path" );
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
			descriptions.add( EXPORT_PHYLOXML, EXPORT_PHYLOXML_KEYS, "Export subtree to PhyloXML format." );
			descriptions.add( FLIP_DESCENDANTS, FLIP_DESCENDANTS_KEYS, "Flip children of the currently selected spot in trackscheme graph." );
			descriptions.add( COPY_TAG, COPY_TAG_KEYS, "Copy tags: everything that has tag A assigned gets B assigned." );
			descriptions.add( INTERPOLATE_SPOTS, INTERPOLATE_SPOTS_KEYS, "Along each track, new spot is inserted to every time points with no spots." );
			descriptions.add( LABEL_SELECTED_SPOTS, LABEL_SELECTED_SPOTS_KEYS, "Set label for all selected spots." );
			descriptions.add( COMPACT_LINEAGE_VIEW, COMPACT_LINEAGE_VIEW_KEYS, "Show compact representation of the lineage tree.");
			descriptions.add( SORT_TREE, SORT_TREE_KEYS, "Sort selected node according to tagged anchors.");
			descriptions.add( MERGE_PROJECTS, MERGE_PROJECTS_KEYS, "Merge two Mastodon projects into one." );
			descriptions.add( TWEAK_DATASET_PATH, TWEAK_DATASET_PATH_KEYS, "Allows to insert new path to the BDV data and whether it is relative or absolute." );
		}
	}

	private final AbstractNamedAction exportPhyloXmlAction;

	private final AbstractNamedAction flipDescendantsAction;

	private final AbstractNamedAction copyTagAction;

	private final AbstractNamedAction interpolateSpotsAction;

	private final AbstractNamedAction labelSelectedSpotsAction;

	private final AbstractNamedAction lineageTreeViewAction;

	private final AbstractNamedAction sortTreeAction;

	private final AbstractNamedAction mergeProjectsAction;

	private final AbstractNamedAction tweakDatasetPathAction;

	private MamutPluginAppModel pluginAppModel;

	public TomancakPlugins()
	{
		exportPhyloXmlAction = new RunnableAction( EXPORT_PHYLOXML, this::exportPhyloXml );
		flipDescendantsAction = new RunnableAction( FLIP_DESCENDANTS, this::flipDescendants );
		copyTagAction = new RunnableAction( COPY_TAG, this::copyTag );
		interpolateSpotsAction = new RunnableAction( INTERPOLATE_SPOTS, this::interpolateSpots );
		labelSelectedSpotsAction = new RunnableAction( LABEL_SELECTED_SPOTS, this::labelSelectedSpots );
		lineageTreeViewAction = new RunnableAction( COMPACT_LINEAGE_VIEW, this::showLineageView );
		sortTreeAction = new RunnableAction( SORT_TREE, this::sortTree );
		mergeProjectsAction = new RunnableAction( MERGE_PROJECTS, this::mergeProjects );
		tweakDatasetPathAction = new RunnableAction( TWEAK_DATASET_PATH, this::tweakDatasetPath );
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
						item( COPY_TAG ),
						menu( "Auxiliary Displays",
								item( COMPACT_LINEAGE_VIEW )),
						menu( "Trees Management",
								item( LABEL_SELECTED_SPOTS ),
								item( INTERPOLATE_SPOTS ),
								item( FLIP_DESCENDANTS ),
								item( SORT_TREE )),
						menu( "Exports",
								item( EXPORT_PHYLOXML )) ),
				menu( "File",
						item( TWEAK_DATASET_PATH ),
						item( MERGE_PROJECTS )) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportPhyloXmlAction, EXPORT_PHYLOXML_KEYS );
		actions.namedAction( flipDescendantsAction, FLIP_DESCENDANTS_KEYS );
		actions.namedAction( copyTagAction, COPY_TAG_KEYS );
		actions.namedAction( interpolateSpotsAction, INTERPOLATE_SPOTS_KEYS );
		actions.namedAction( labelSelectedSpotsAction, LABEL_SELECTED_SPOTS_KEYS );
		actions.namedAction( lineageTreeViewAction, COMPACT_LINEAGE_VIEW_KEYS );
		actions.namedAction( sortTreeAction, SORT_TREE_KEYS );
		actions.namedAction( mergeProjectsAction, MERGE_PROJECTS_KEYS );
		actions.namedAction( tweakDatasetPathAction, TWEAK_DATASET_PATH_KEYS );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		exportPhyloXmlAction.setEnabled( appModel != null );
		flipDescendantsAction.setEnabled( appModel != null );
		copyTagAction.setEnabled( appModel != null );
		interpolateSpotsAction.setEnabled( appModel != null );
		labelSelectedSpotsAction.setEnabled( appModel != null );
		lineageTreeViewAction.setEnabled( appModel != null );
		sortTreeAction.setEnabled( appModel != null );
		mergeProjectsAction.setEnabled( appModel != null );
		tweakDatasetPathAction.setEnabled( appModel != null );
	}

	private void exportPhyloXml()
	{
		if ( pluginAppModel != null )
			MakePhyloXml.exportSelectedSubtreeToPhyloXmlFile( pluginAppModel.getAppModel() );
	}

	private void flipDescendants()
	{
		if ( pluginAppModel != null )
			FlipDescendants.flipDescendants( pluginAppModel.getAppModel() );
	}

	private void copyTag()
	{
		if ( pluginAppModel != null )
		{
			final Model model = pluginAppModel.getAppModel().getModel();
			new CopyTagDialog( null, model ).setVisible( true );
		}
	}

	private void interpolateSpots()
	{
		if ( pluginAppModel != null )
		{
			final Model model = pluginAppModel.getAppModel().getModel();
			InterpolateMissingSpots.interpolate( model );
		}
	}

	private void labelSelectedSpots()
	{
		if ( pluginAppModel != null )
		{
			final MamutAppModel appModel = pluginAppModel.getAppModel();
			final SelectionModel< Spot, Link > selection = appModel.getSelectionModel();
			final Model model = appModel.getModel();
			final ReentrantReadWriteLock lock = model.getGraph().getLock();
			lock.writeLock().lock();
			try
			{
				final Set< Spot > spots = selection.getSelectedVertices();
				if ( spots.isEmpty() )
				{
					JOptionPane.showMessageDialog( null, "No spot selected.", "Label spots", JOptionPane.WARNING_MESSAGE );
				}
				else
				{
					final String initialValue = spots.iterator().next().getLabel();
					final Object input = JOptionPane.showInputDialog( null, "Spot label:", "Label spots", JOptionPane.PLAIN_MESSAGE, null, null, initialValue );
					if ( input != null )
					{
						final String label = ( String ) input;
						spots.forEach( spot -> spot.setLabel( label ) );
						model.setUndoPoint();
					}
				}
			}
			finally
			{
				lock.writeLock().unlock();
			}
		}
	}

	private void sortTree() {
		SortTreeDialog.showDialog( pluginAppModel.getAppModel() );
	}

	private void showLineageView() {
		if( pluginAppModel == null )
			return;
		CompactLineageFrame frame =
			new CompactLineageFrame(pluginAppModel.getAppModel());
		frame.setVisible(true);
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

	private void tweakDatasetPath()
	{
		if ( pluginAppModel != null )
		{
			final MamutProject project = pluginAppModel.getWindowManager().getProjectManager().getProject();
			new DatasetPathDialog( null, project ).setVisible( true );
		}
	}
}
