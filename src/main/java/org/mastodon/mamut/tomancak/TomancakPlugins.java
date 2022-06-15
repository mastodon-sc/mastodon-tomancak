/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.tomancak.label_systematically.LabelSpotsSystematicallyDialog;
import org.mastodon.mamut.tomancak.compact_lineage.CompactLineageFrame;
import org.mastodon.mamut.tomancak.export.ExportCounts;
import org.mastodon.mamut.tomancak.export.LineageLengthExporter;
import org.mastodon.mamut.tomancak.export.MakePhyloXml;
import org.mastodon.mamut.tomancak.merging.Dataset;
import org.mastodon.mamut.tomancak.merging.MergeDatasets;
import org.mastodon.mamut.tomancak.merging.MergingDialog;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeLeftRightDialog;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeExternInternDialog;
import org.mastodon.mamut.tomancak.spots.FilterOutSolists;
import org.mastodon.mamut.tomancak.spots.InterpolateMissingSpots;
import org.mastodon.mamut.tomancak.lineage_registration.LineageRegistrationPlugin;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.command.CommandService;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = MamutPlugin.class )
public class TomancakPlugins extends AbstractContextual implements MamutPlugin
{
	private static final String EXPORT_PHYLOXML = "[tomancak] export phyloxml for selection";
	private static final String FLIP_DESCENDANTS = "[tomancak] flip lineage descendants";
	private static final String COPY_TAG = "[tomancak] copy tag";
	private static final String INTERPOLATE_SPOTS = "[tomancak] interpolate missing spots";
	private static final String LABEL_SELECTED_SPOTS = "[tomancak] label selected spots";
	private static final String CHANGE_BRANCH_LABELS = "[tomancak] change branch labels";
	private static final String COMPACT_LINEAGE_VIEW = "[tomancak] show compact lineage";
	private static final String SORT_TREE = "[tomancak] sort lineage tree";
	private static final String SORT_TREE_EXTERN_INTERN = "[tomancak] sort lineage tree extern intern";
	private static final String MATCH_TREE = "[tomancak] match tree to other project";
	private static final String LABEL_SPOTS_SYSTEMATICALLY = "[tomancak] label spots systematically";
	private static final String REMOVE_SOLISTS_SPOTS = "[tomancak] remove solists spots";
	private static final String EXPORTS_LINEAGE_LENGTHS = "[tomancak] export lineage lengths";
	private static final String EXPORTS_SPOTS_COUNTS = "[tomancak] export spots counts";
	private static final String MERGE_PROJECTS = "[tomancak] merge projects";
	private static final String TWEAK_DATASET_PATH = "[tomancak] fix project image path";

	private static final String[] EXPORT_PHYLOXML_KEYS = { "not mapped" };
	private static final String[] FLIP_DESCENDANTS_KEYS = { "ctrl E" };
	private static final String[] COPY_TAG_KEYS = { "not mapped" };
	private static final String[] INTERPOLATE_SPOTS_KEYS = { "not mapped" };
	private static final String[] LABEL_SELECTED_SPOTS_KEYS = { "F2" };
	private static final String[] CHANGE_BRANCH_LABELS_KEYS = { "shift F2" };
	private static final String[] COMPACT_LINEAGE_VIEW_KEYS = { "not mapped" };
	private static final String[] SORT_TREE_KEYS = { "ctrl S" };
	private static final String[] SORT_TREE_EXTERN_INTERN_KEYS = { "not mapped" };
	private static final String[] MATCH_TREE_KEYS = { "not mapped"};
	private static final String[] LABEL_SPOTS_SYSTEMATICALLY_KEYS = { "not mapped" };
	private static final String[] REMOVE_SOLISTS_SPOTS_KEYS = { "not mapped" };
	private static final String[] EXPORTS_LINEAGE_LENGTHS_KEYS = { "not mapped" };
	private static final String[] EXPORTS_SPOTS_COUNTS_KEYS = { "not mapped" };
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
		menuTexts.put( CHANGE_BRANCH_LABELS, "Change Branch's Labels");
		menuTexts.put( COMPACT_LINEAGE_VIEW, "Show Compact Lineage" );
		menuTexts.put( SORT_TREE, "Sort Lineage Tree" );
		menuTexts.put( SORT_TREE_EXTERN_INTERN, "Sort Lineage Tree (Extern-Intern)");
		menuTexts.put( MATCH_TREE, "Sort Lineage Tree (To Match Other Project)" );
		menuTexts.put( LABEL_SPOTS_SYSTEMATICALLY, "Systematically Label Spots (Extern-Intern)" );
		menuTexts.put( REMOVE_SOLISTS_SPOTS, "Remove Spots Solists" );
		menuTexts.put( EXPORTS_LINEAGE_LENGTHS, "Export Lineage Lengths" );
		menuTexts.put( EXPORTS_SPOTS_COUNTS, "Export Spots Counts" );
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
			descriptions.add( CHANGE_BRANCH_LABELS, CHANGE_BRANCH_LABELS_KEYS, "Change the labels of all the spots between to division." );
			descriptions.add( COMPACT_LINEAGE_VIEW, COMPACT_LINEAGE_VIEW_KEYS, "Show compact representation of the lineage tree.");
			descriptions.add( SORT_TREE, SORT_TREE_KEYS, "Sort selected node according to tagged anchors.");
			descriptions.add( SORT_TREE_EXTERN_INTERN, SORT_TREE_EXTERN_INTERN_KEYS, "Sort selected nodes according to tagged center anchor.");
			descriptions.add( MATCH_TREE, MATCH_TREE_KEYS, "Sort the TrackScheme such that the order matches another project.");
			descriptions.add( LABEL_SPOTS_SYSTEMATICALLY, LABEL_SPOTS_SYSTEMATICALLY_KEYS, "Child cells are named after their parent cell, with a \"1\" or \"2\" appended to the label.");
			descriptions.add( REMOVE_SOLISTS_SPOTS, REMOVE_SOLISTS_SPOTS_KEYS, "Finds and removes isolated spots from the lineage, based on conditions." );
			descriptions.add( EXPORTS_LINEAGE_LENGTHS, EXPORTS_LINEAGE_LENGTHS_KEYS, "Exports lineage lengths into CSV-like files to be imported in data processors." );
			descriptions.add( EXPORTS_SPOTS_COUNTS, EXPORTS_SPOTS_COUNTS_KEYS, "Exports counts of spots into CSV-like files to be imported in data processors." );
			descriptions.add( MERGE_PROJECTS, MERGE_PROJECTS_KEYS, "Merge two Mastodon projects into one." );
			descriptions.add( TWEAK_DATASET_PATH, TWEAK_DATASET_PATH_KEYS, "Allows to insert new path to the BDV data and whether it is relative or absolute." );
		}
	}

	private final AbstractNamedAction exportPhyloXmlAction;

	private final AbstractNamedAction flipDescendantsAction;

	private final AbstractNamedAction copyTagAction;

	private final AbstractNamedAction interpolateSpotsAction;

	private final AbstractNamedAction labelSelectedSpotsAction;

	private final AbstractNamedAction changeBranchLabelsAction;

	private final AbstractNamedAction lineageTreeViewAction;

	private final AbstractNamedAction sortTreeAction;

	private final AbstractNamedAction sortTreeExternInternAction;

	private final AbstractNamedAction matchTreeAction;
	
	private final AbstractNamedAction labelSpotsSystematicallyAction;

	private final AbstractNamedAction removeSolistsAction;

	private final AbstractNamedAction exportLineageLengthsAction;

	private final AbstractNamedAction exportSpotsCountsAction;

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
		changeBranchLabelsAction = new RunnableAction( CHANGE_BRANCH_LABELS, this::changeBranchLabels );
		lineageTreeViewAction = new RunnableAction( COMPACT_LINEAGE_VIEW, this::showLineageView );
		sortTreeAction = new RunnableAction( SORT_TREE, this::sortTree );
		sortTreeExternInternAction = new RunnableAction( SORT_TREE_EXTERN_INTERN, this::sortTreeExternIntern );
		matchTreeAction = new RunnableAction( MATCH_TREE, this::matchTree );
		labelSpotsSystematicallyAction = new RunnableAction( LABEL_SPOTS_SYSTEMATICALLY, this::labelSpotsSystematically );
		removeSolistsAction = new RunnableAction( REMOVE_SOLISTS_SPOTS, this::filterOutSolists );
		exportLineageLengthsAction = new RunnableAction( EXPORTS_LINEAGE_LENGTHS, this::exportLengths );
		exportSpotsCountsAction = new RunnableAction( EXPORTS_SPOTS_COUNTS, this::exportCounts );
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
								item( CHANGE_BRANCH_LABELS ),
								item( REMOVE_SOLISTS_SPOTS ),
								item( INTERPOLATE_SPOTS ),
								item( FLIP_DESCENDANTS ),
								item( SORT_TREE ),
								item( SORT_TREE_EXTERN_INTERN ),
								item( MATCH_TREE )),
								item( LABEL_SPOTS_SYSTEMATICALLY ),
				menu( "Exports",
								item( EXPORTS_LINEAGE_LENGTHS ),
								item( EXPORTS_SPOTS_COUNTS ),
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
		actions.namedAction( changeBranchLabelsAction, CHANGE_BRANCH_LABELS_KEYS );
		actions.namedAction( lineageTreeViewAction, COMPACT_LINEAGE_VIEW_KEYS );
		actions.namedAction( sortTreeAction, SORT_TREE_KEYS );
		actions.namedAction( sortTreeExternInternAction, SORT_TREE_EXTERN_INTERN_KEYS );
		actions.namedAction( labelSpotsSystematicallyAction, LABEL_SPOTS_SYSTEMATICALLY_KEYS );
		actions.namedAction( removeSolistsAction, REMOVE_SOLISTS_SPOTS_KEYS );
		actions.namedAction( exportLineageLengthsAction, EXPORTS_LINEAGE_LENGTHS_KEYS );
		actions.namedAction( exportSpotsCountsAction, EXPORTS_SPOTS_COUNTS_KEYS );
		actions.namedAction( mergeProjectsAction, MERGE_PROJECTS_KEYS );
		actions.namedAction( tweakDatasetPathAction, TWEAK_DATASET_PATH_KEYS );
		actions.namedAction( matchTreeAction, MATCH_TREE_KEYS );
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
		sortTreeExternInternAction.setEnabled( appModel != null );
		labelSpotsSystematicallyAction.setEnabled( appModel != null );
		removeSolistsAction.setEnabled( appModel != null );
		exportLineageLengthsAction.setEnabled( appModel != null );
		exportSpotsCountsAction.setEnabled( appModel != null );
		mergeProjectsAction.setEnabled( appModel != null );
		tweakDatasetPathAction.setEnabled( appModel != null );
		matchTreeAction.setEnabled( appModel!= null );
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
			LabelSelectedSpots.labelSelectedSpot( appModel );
		}
	}

	private void sortTree() {
		SortTreeLeftRightDialog.showDialog( pluginAppModel.getAppModel() );
	}

	private void sortTreeExternIntern()
	{
		SortTreeExternInternDialog.showDialog( pluginAppModel.getAppModel() );
	}

	private void showLineageView() {
		if( pluginAppModel == null )
			return;
		CompactLineageFrame frame =
			new CompactLineageFrame(pluginAppModel.getAppModel());
		frame.setVisible(true);
	}

	private void filterOutSolists()
	{
		this.getContext().getService(CommandService.class).run(
				FilterOutSolists.class, true,
				"appModel", pluginAppModel.getAppModel());
	}

	private void exportLengths()
	{
		this.getContext().getService(CommandService.class).run(
				LineageLengthExporter.class, true,
				"appModel", pluginAppModel.getAppModel());
	}

	private void exportCounts()
	{
		this.getContext().getService(CommandService.class).run(
				ExportCounts.class, true,
				"appModel", pluginAppModel.getAppModel());
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
			new DatasetPathDialog( null, pluginAppModel ).setVisible( true );
		}
	}

	private void changeBranchLabels()
	{
		if ( pluginAppModel != null ) {
			RenameBranchLabels.run(pluginAppModel);
		}
	}

	private void labelSpotsSystematically()
	{
		if ( pluginAppModel != null ) {
			LabelSpotsSystematicallyDialog.showDialog( pluginAppModel.getAppModel() );
		}
	}
	
	private void matchTree()
	{
		if ( pluginAppModel != null )
			LineageRegistrationPlugin.showDialog( pluginAppModel.getAppModel() );
	}
}
