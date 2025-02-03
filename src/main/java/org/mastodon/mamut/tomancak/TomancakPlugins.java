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

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.tomancak.compact_lineage.CompactLineageFrame;
import org.mastodon.mamut.tomancak.divisioncount.ShowDivisionCountsOverTimeCommand;
import org.mastodon.mamut.tomancak.divisiontagset.CellDivisionsTagSetCommand;
import org.mastodon.mamut.tomancak.export.ExportCounts;
import org.mastodon.mamut.tomancak.export.ExportDivisionCountsPerTimepointCommand;
import org.mastodon.mamut.tomancak.export.ExportSpotCountsPerTimepointCommand;
import org.mastodon.mamut.tomancak.export.LineageLengthExporter;
import org.mastodon.mamut.tomancak.export.MakePhyloXml;
import org.mastodon.mamut.tomancak.label_systematically.LabelSpotsSystematicallyDialog;
import org.mastodon.mamut.tomancak.resolve.CreateConflictTagSetCommand;
import org.mastodon.mamut.tomancak.resolve.FuseSpots;
import org.mastodon.mamut.tomancak.resolve.LocateTagsFrame;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;
import org.mastodon.mamut.tomancak.sort_tree.SortTree;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeExternInternDialog;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeLeftRightDialog;
import org.mastodon.mamut.tomancak.spots.AddCenterSpots;
import org.mastodon.mamut.tomancak.spots.FilterOutIsolatedSpots;
import org.mastodon.mamut.tomancak.spots.InterpolateMissingSpots;
import org.mastodon.mamut.tomancak.spots.MirrorEmbryo;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = MamutPlugin.class )
public class TomancakPlugins extends AbstractContextual implements MamutPlugin
{

	private static final String EXPORT_PHYLOXML = "[tomancak] export phyloxml for selected spot";
	private static final String FLIP_DESCENDANTS = "[tomancak] flip lineage descendants";
	private static final String COPY_TAG = "[tomancak] copy tag";
	private static final String INTERPOLATE_SPOTS = "[tomancak] interpolate missing spots";
	private static final String LABEL_SELECTED_SPOTS = "[tomancak] label selected spots";
	private static final String SET_RADIUS_SELECTED_SPOTS = "[tomancak] set radius selected spots";
	private static final String CHANGE_BRANCH_LABELS = "[tomancak] change branch labels";
	private static final String COMPACT_LINEAGE_VIEW = "[tomancak] show compact lineage";
	private static final String SORT_TREE = "[tomancak] sort lineage tree";
	private static final String SORT_TREE_EXTERN_INTERN = "[tomancak] sort lineage tree extern intern";
	private static final String SORT_TREE_LIFETIME = "[tomancak] sort lineage tree lifetime";
	private static final String LABEL_SPOTS_SYSTEMATICALLY = "[tomancak] label spots systematically";

	private static final String REMOVE_ISOLATED_SPOTS = "[tomancak] remove isolated spots";
	private static final String EXPORTS_LINEAGE_LENGTHS = "[tomancak] export lineage lengths";

	private static final String EXPORT_SPOTS_COUNTS_PER_LINEAGE = "[tomancak] export spot counts per lineage";

	private static final String EXPORT_SPOTS_COUNTS_PER_TIMEPOINT = "[tomancak] export spot counts per timepoint";

	private static final String EXPORT_DIVISION_COUNTS_PER_TIMEPOINT = "[tomancak] export division counts per timepoint";

	private static final String SHOW_DIVISION_COUNTS_OVER_TIME = "[tomancak] show division counts over time";

	private static final String ADD_CENTER_SPOTS = "[tomancak] add center spots";
	private static final String MIRROR_SPOTS = "[tomancak] mirror spots";
	private static final String CREATE_CONFLICT_TAG_SET = "[tomancak] create conflict tag set";
	private static final String FUSE_SPOTS = "[tomancak] fuse selected spots";
	private static final String LOCATE_TAGS = "[tomancak] locate tags";
	private static final String CELL_DIVISIONS_TAG_SET = "[tomancak] create cell divisions tag set";
	private static final String CREATE_DUMMY_TAG_SET = "[tomancak] create dummy tag set";

	private static final String[] EXPORT_PHYLOXML_KEYS = { "not mapped" };
	private static final String[] FLIP_DESCENDANTS_KEYS = { "ctrl E" };
	private static final String[] COPY_TAG_KEYS = { "not mapped" };
	private static final String[] INTERPOLATE_SPOTS_KEYS = { "not mapped" };
	private static final String[] LABEL_SELECTED_SPOTS_KEYS = { "F2" };
	private static final String[] SET_RADIUS_SELECTED_SPOTS_KEYS = { "F3" };
	private static final String[] CHANGE_BRANCH_LABELS_KEYS = { "shift F2" };
	private static final String[] COMPACT_LINEAGE_VIEW_KEYS = { "not mapped" };
	private static final String[] SORT_TREE_KEYS = { "ctrl S" };
	private static final String[] SORT_TREE_EXTERN_INTERN_KEYS = { "not mapped" };
	private static final String[] SORT_TREE_LIFETIME_KEYS = { "not mapped" };
	private static final String[] LABEL_SPOTS_SYSTEMATICALLY_KEYS = { "not mapped" };

	private static final String[] REMOVE_ISOLATED_SPOTS_KEYS = { "not mapped" };
	private static final String[] EXPORTS_LINEAGE_LENGTHS_KEYS = { "not mapped" };
	private static final String[] EXPORTS_SPOTS_COUNTS_PER_LINEAGE_KEYS = { "not mapped" };
	private static final String[] EXPORTS_SPOTS_COUNTS_PER_TIMEPOINT_KEYS = { "not mapped" };

	private static final String[] EXPORT_DIVISION_COUNTS_PER_TIMEPOINT_KEYS = { "not mapped" };

	private static final String[] SHOW_DIVISION_COUNTS_OVER_TIME_KEYS = { "not mapped" };

	private static final String[] ADD_CENTER_SPOTS_KEYS = { "not mapped" };
	private static final String[] MIRROR_SPOTS_KEYS = { "not mapped" };
	private static final String[] CREATE_CONFLICT_TAG_SET_KEYS = { "not mapped" };
	private static final String[] FUSE_SPOTS_KEYS = { "ctrl alt F" };
	private static final String[] LOCATE_TAGS_KEYS = { "not mapped" };
	private static final String[] CELL_DIVISIONS_TAG_SET_KEYS = { "not mapped" };
	private static final String[] CREATE_DUMMY_TAG_SET_KEYS = { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( EXPORT_PHYLOXML, "Export phyloXML for selected spot" );
		menuTexts.put( FLIP_DESCENDANTS, "Flip descendants" );
		menuTexts.put( COPY_TAG, "Copy tag" );
		menuTexts.put( INTERPOLATE_SPOTS, "Interpolate missing spots" );
		menuTexts.put( LABEL_SELECTED_SPOTS, "Label selected spots" );
		menuTexts.put( SET_RADIUS_SELECTED_SPOTS, "Set radius of selected spots" );
		menuTexts.put( CHANGE_BRANCH_LABELS, "Change branch labels" );
		menuTexts.put( COMPACT_LINEAGE_VIEW, "Show compact lineage" );
		menuTexts.put( SORT_TREE, "Sort lineage tree (left-right-landmarks)" );
		menuTexts.put( SORT_TREE_EXTERN_INTERN, "Sort lineage tree (extern-intern)" );
		menuTexts.put( SORT_TREE_LIFETIME, "Sort lineage tree (cell life cycle duration)" );
		menuTexts.put( LABEL_SPOTS_SYSTEMATICALLY, "Systematically label spots (extern-intern)" );
		menuTexts.put( REMOVE_ISOLATED_SPOTS, "Remove isolated spots" );
		menuTexts.put( EXPORTS_LINEAGE_LENGTHS, "Export lineage lengths" );
		menuTexts.put( EXPORT_SPOTS_COUNTS_PER_LINEAGE, "Export spot counts per lineage" );
		menuTexts.put( EXPORT_SPOTS_COUNTS_PER_TIMEPOINT, "Export spot counts per timepoint" );
		menuTexts.put( EXPORT_DIVISION_COUNTS_PER_TIMEPOINT, "Export division counts per timepoint" );
		menuTexts.put( SHOW_DIVISION_COUNTS_OVER_TIME, "Show division counts over time" );
		menuTexts.put( ADD_CENTER_SPOTS, "Add center spots" );
		menuTexts.put( MIRROR_SPOTS, "Mirror spots along X-axis" );
		menuTexts.put( CREATE_CONFLICT_TAG_SET, "Create conflict tag set" );
		menuTexts.put( FUSE_SPOTS, "Fuse selected spots" );
		menuTexts.put( LOCATE_TAGS, "Locate tags" );
		menuTexts.put( CELL_DIVISIONS_TAG_SET, "Add tag set to highlight cell divisions" );
		menuTexts.put( CREATE_DUMMY_TAG_SET, "Create dummy tag set" );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( EXPORT_PHYLOXML, EXPORT_PHYLOXML_KEYS, "Export subtree to PhyloXML format." );
			descriptions.add( FLIP_DESCENDANTS, FLIP_DESCENDANTS_KEYS, "Flip children of the currently selected spot in trackscheme graph." );
			descriptions.add( COPY_TAG, COPY_TAG_KEYS, "Copy tags: everything that has tag A assigned gets B assigned." );
			descriptions.add( INTERPOLATE_SPOTS, INTERPOLATE_SPOTS_KEYS, "Along each track, new spot is inserted to every time points with no spots." );
			descriptions.add( LABEL_SELECTED_SPOTS, LABEL_SELECTED_SPOTS_KEYS, "Set label for all selected spots." );
			descriptions.add( SET_RADIUS_SELECTED_SPOTS, SET_RADIUS_SELECTED_SPOTS_KEYS, "Set radius for all selected spots." );
			descriptions.add( CHANGE_BRANCH_LABELS, CHANGE_BRANCH_LABELS_KEYS, "Change the labels of all the spots between to division." );
			descriptions.add( COMPACT_LINEAGE_VIEW, COMPACT_LINEAGE_VIEW_KEYS, "Show compact representation of the lineage tree.");
			descriptions.add( SORT_TREE, SORT_TREE_KEYS, "Sort selected spots according to selectable landmarks." );
			descriptions.add( SORT_TREE_EXTERN_INTERN, SORT_TREE_EXTERN_INTERN_KEYS, "Sort selected spots according to tagged center anchor.");
			descriptions.add( SORT_TREE_LIFETIME, SORT_TREE_LIFETIME_KEYS, "Sort selected spots, such that the child cell with the longer cell cycle duration is left in the TrackScheme.");
			descriptions.add( LABEL_SPOTS_SYSTEMATICALLY, LABEL_SPOTS_SYSTEMATICALLY_KEYS, "Child cells are named after their parent cell, with a \"1\" or \"2\" appended to the label.");
			descriptions.add( REMOVE_ISOLATED_SPOTS, REMOVE_ISOLATED_SPOTS_KEYS,
					"Finds and removes isolated spots from the lineage, based on conditions." );
			descriptions.add( EXPORTS_LINEAGE_LENGTHS, EXPORTS_LINEAGE_LENGTHS_KEYS, "Exports lineage lengths into CSV-like files to be imported in data processors." );
			descriptions.add( EXPORT_SPOTS_COUNTS_PER_LINEAGE, EXPORTS_SPOTS_COUNTS_PER_LINEAGE_KEYS,
					"Exports counts of spots into CSV-like files to be imported in data processors. One file per lineage." );
			descriptions.add( EXPORT_SPOTS_COUNTS_PER_TIMEPOINT, EXPORTS_SPOTS_COUNTS_PER_TIMEPOINT_KEYS,
					"Exports counts of spots per timepoint into CSV-like files to be imported in data processors. One file." );
			descriptions.add( EXPORT_DIVISION_COUNTS_PER_TIMEPOINT, EXPORT_DIVISION_COUNTS_PER_TIMEPOINT_KEYS,
					"Exports counts of divisions per timepoint into CSV-like files to be imported in data processors. One file." );
			descriptions.add( SHOW_DIVISION_COUNTS_OVER_TIME, SHOW_DIVISION_COUNTS_OVER_TIME_KEYS,
					"Shows a plot of the number of divisions over time." );
			descriptions.add( ADD_CENTER_SPOTS, ADD_CENTER_SPOTS_KEYS, "On each timepoint with selected spots, add a new spot that is in the center (average position)." );
			descriptions.add( MIRROR_SPOTS, MIRROR_SPOTS_KEYS, "Mirror spots along x-axis." );
			descriptions.add( CREATE_CONFLICT_TAG_SET, CREATE_CONFLICT_TAG_SET_KEYS, "Search spots that overlap and create a tag set that highlights these conflicts." );
			descriptions.add( FUSE_SPOTS, FUSE_SPOTS_KEYS, "Fuse selected spots into a single spot. Average spot position and shape." );
			descriptions.add( LOCATE_TAGS, LOCATE_TAGS_KEYS, "Open a dialog that allows to jump to specific tags." );
			descriptions.add( CELL_DIVISIONS_TAG_SET, CELL_DIVISIONS_TAG_SET_KEYS, "Adds a tag set to highlight cell divisions." );
			descriptions.add( CREATE_DUMMY_TAG_SET, CREATE_DUMMY_TAG_SET_KEYS,
					"Creates a dummy tag set with a specifiable maximum number of tags." );
		}
	}

	@Parameter
	private CommandService commandService;

	private final AbstractNamedAction exportPhyloXmlAction;

	private final AbstractNamedAction flipDescendantsAction;

	private final AbstractNamedAction copyTagAction;

	private final AbstractNamedAction interpolateSpotsAction;

	private final AbstractNamedAction labelSelectedSpotsAction;

	private final AbstractNamedAction setRadiusSelectedSpotsAction;

	private final AbstractNamedAction changeBranchLabelsAction;

	private final AbstractNamedAction lineageTreeViewAction;

	private final AbstractNamedAction sortTreeAction;

	private final AbstractNamedAction sortTreeExternInternAction;

	private final AbstractNamedAction sortTreeLifetimeAction;

	private final AbstractNamedAction labelSpotsSystematicallyAction;

	private final AbstractNamedAction removeIsolatedSpotsAction;

	private final AbstractNamedAction exportLineageLengthsAction;

	private final AbstractNamedAction exportSpotsCountsPerLineageAction;

	private final AbstractNamedAction exportSpotsCountsPerTimepointAction;

	private final AbstractNamedAction exportDivisionCountsPerTimepointAction;

	private final AbstractNamedAction showDivisionCountsOverTimeAction;

	// private final AbstractNamedAction mergeProjectsAction;

	private final AbstractNamedAction addCenterSpots;

	private final AbstractNamedAction mirrorSpots;

	private final AbstractNamedAction createConflictTagSet;

	private final AbstractNamedAction fuseSpots;

	private final AbstractNamedAction locateTags;

	private final AbstractNamedAction cellDivisionsTagSetAction;

	private final AbstractNamedAction createDummyTagSet;

	private ProjectModel projectModel;

	public TomancakPlugins()
	{
		exportPhyloXmlAction = new RunnableAction( EXPORT_PHYLOXML, this::exportPhyloXml );
		flipDescendantsAction = new RunnableAction( FLIP_DESCENDANTS, this::flipDescendants );
		copyTagAction = new RunnableAction( COPY_TAG, this::copyTag );
		interpolateSpotsAction = new RunnableAction( INTERPOLATE_SPOTS, this::interpolateSpots );
		labelSelectedSpotsAction = new RunnableAction( LABEL_SELECTED_SPOTS, this::labelSelectedSpots );
		setRadiusSelectedSpotsAction = new RunnableAction( SET_RADIUS_SELECTED_SPOTS, this::setRadiusSelectedSpots );
		changeBranchLabelsAction = new RunnableAction( CHANGE_BRANCH_LABELS, this::changeBranchLabels );
		lineageTreeViewAction = new RunnableAction( COMPACT_LINEAGE_VIEW, this::showLineageView );
		sortTreeAction = new RunnableAction( SORT_TREE, this::sortTree );
		sortTreeExternInternAction = new RunnableAction( SORT_TREE_EXTERN_INTERN, this::sortTreeExternIntern );
		sortTreeLifetimeAction = new RunnableAction( SORT_TREE_LIFETIME, this::sortTreeCellLifetime );
		labelSpotsSystematicallyAction = new RunnableAction( LABEL_SPOTS_SYSTEMATICALLY, this::labelSpotsSystematically );
		removeIsolatedSpotsAction = new RunnableAction( REMOVE_ISOLATED_SPOTS, this::filterOutIsolatedSpots );
		exportLineageLengthsAction = new RunnableAction( EXPORTS_LINEAGE_LENGTHS, this::exportLengths );
		exportSpotsCountsPerLineageAction = new RunnableAction( EXPORT_SPOTS_COUNTS_PER_LINEAGE, this::exportCountsPerLineage );
		exportSpotsCountsPerTimepointAction = new RunnableAction( EXPORT_SPOTS_COUNTS_PER_TIMEPOINT, this::exportCountsPerTimepoint );
		exportDivisionCountsPerTimepointAction =
				new RunnableAction( EXPORT_DIVISION_COUNTS_PER_TIMEPOINT, this::exportDivisionCountsPerTimepoint );
		showDivisionCountsOverTimeAction = new RunnableAction( SHOW_DIVISION_COUNTS_OVER_TIME, this::showDivisionCountsOverTime );
		addCenterSpots = new RunnableAction( ADD_CENTER_SPOTS, this::addCenterSpots );
		mirrorSpots = new RunnableAction( MIRROR_SPOTS, this::mirrorSpots );
		createConflictTagSet = new RunnableAction( CREATE_CONFLICT_TAG_SET, this::createConflictTagSet );
		fuseSpots = new RunnableAction( FUSE_SPOTS, this::fuseSpots );
		locateTags = new RunnableAction( LOCATE_TAGS, this::locateTags );
		cellDivisionsTagSetAction = new RunnableAction( CELL_DIVISIONS_TAG_SET, this::runCellDivisionsTagSet );
		createDummyTagSet = new RunnableAction( CREATE_DUMMY_TAG_SET, this::createDummyTagSet );
	}

	@Override
	public void setAppPluginModel( final ProjectModel model )
	{
		this.projectModel = model;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "File",
						menu( "Export",
								menu( "Export measurements",
										menu( "Spot counts",
												item( EXPORT_SPOTS_COUNTS_PER_LINEAGE ),
												item( EXPORT_SPOTS_COUNTS_PER_TIMEPOINT ) ),
										item( EXPORT_DIVISION_COUNTS_PER_TIMEPOINT ) ),
								// item( EXPORTS_LINEAGE_LENGTHS ) ), // NB: deactivated for now, since the function is too prototype-y
								item( EXPORT_PHYLOXML ) ) ),
				menu( "Plugins",
						menu( "Lineage analysis",
								item( SHOW_DIVISION_COUNTS_OVER_TIME ) ),
						menu( "Tags",
								item( LOCATE_TAGS ),
								item( COPY_TAG ),
								item( CELL_DIVISIONS_TAG_SET ),
								item( CREATE_DUMMY_TAG_SET ) ),
						menu( "Spots management",
								menu( "Rename spots",
										item( LABEL_SELECTED_SPOTS ),
										item( CHANGE_BRANCH_LABELS ),
										item( LABEL_SPOTS_SYSTEMATICALLY ) ),
								menu( "Transform spots",
										item( MIRROR_SPOTS ),
										item( REMOVE_ISOLATED_SPOTS ),
										item( ADD_CENTER_SPOTS ),
										item( INTERPOLATE_SPOTS ),
										item( SET_RADIUS_SELECTED_SPOTS ) ) ),
						menu( "Trees management",
								item( FLIP_DESCENDANTS ),
								menu( "Conflict resolution",
										item( CREATE_CONFLICT_TAG_SET ),
										item( FUSE_SPOTS ) ),
								menu( "Sort trackscheme",
										item( SORT_TREE ),
										item( SORT_TREE_EXTERN_INTERN ),
										item( SORT_TREE_LIFETIME ) ),
								item( COMPACT_LINEAGE_VIEW ) ) ) );
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
		actions.namedAction( setRadiusSelectedSpotsAction, SET_RADIUS_SELECTED_SPOTS_KEYS );
		actions.namedAction( changeBranchLabelsAction, CHANGE_BRANCH_LABELS_KEYS );
		actions.namedAction( lineageTreeViewAction, COMPACT_LINEAGE_VIEW_KEYS );
		actions.namedAction( sortTreeAction, SORT_TREE_KEYS );
		actions.namedAction( sortTreeExternInternAction, SORT_TREE_EXTERN_INTERN_KEYS );
		actions.namedAction( sortTreeLifetimeAction, SORT_TREE_LIFETIME_KEYS );
		actions.namedAction( labelSpotsSystematicallyAction, LABEL_SPOTS_SYSTEMATICALLY_KEYS );
		actions.namedAction( removeIsolatedSpotsAction, REMOVE_ISOLATED_SPOTS_KEYS );
		actions.namedAction( exportLineageLengthsAction, EXPORTS_LINEAGE_LENGTHS_KEYS );
		actions.namedAction( exportSpotsCountsPerLineageAction, EXPORTS_SPOTS_COUNTS_PER_LINEAGE_KEYS );
		actions.namedAction( exportSpotsCountsPerTimepointAction, EXPORTS_SPOTS_COUNTS_PER_TIMEPOINT_KEYS );
		actions.namedAction( exportDivisionCountsPerTimepointAction, EXPORT_DIVISION_COUNTS_PER_TIMEPOINT_KEYS );
		actions.namedAction( showDivisionCountsOverTimeAction, SHOW_DIVISION_COUNTS_OVER_TIME_KEYS );
		actions.namedAction( addCenterSpots, ADD_CENTER_SPOTS_KEYS );
		actions.namedAction( mirrorSpots, MIRROR_SPOTS_KEYS );
		actions.namedAction( createConflictTagSet, CREATE_CONFLICT_TAG_SET_KEYS );
		actions.namedAction( fuseSpots, FUSE_SPOTS_KEYS );
		actions.namedAction( locateTags, LOCATE_TAGS_KEYS );
		actions.namedAction( cellDivisionsTagSetAction, CELL_DIVISIONS_TAG_SET_KEYS );
		actions.namedAction( createDummyTagSet, CREATE_DUMMY_TAG_SET_KEYS );
	}

	private void exportPhyloXml()
	{
		MakePhyloXml.exportSelectedSubtreeToPhyloXmlFile( projectModel );
	}

	private void flipDescendants()
	{
		FlipDescendants.flipDescendants( projectModel );
	}

	private void copyTag()
	{
		final Model model = projectModel.getModel();
		new CopyTagDialog( null, model ).setVisible( true );
	}

	private void interpolateSpots()
	{
		final Model model = projectModel.getModel();
		InterpolateMissingSpots.interpolate( model );
	}

	private void labelSelectedSpots()
	{
		LabelSelectedSpots.labelSelectedSpot( projectModel );
	}

	private void setRadiusSelectedSpots()
	{
		SetRadiusSelectedSpots.setRadiusSelectedSpot( projectModel, commandService );
	}

	private void sortTree() {
		SortTreeLeftRightDialog.showDialog( projectModel );
	}

	private void sortTreeExternIntern()
	{
		SortTreeExternInternDialog.showDialog( projectModel );
	}

	private void sortTreeCellLifetime()
	{
		final ProjectModel appModel = projectModel;
		final Model model = appModel.getModel();
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();

		Collection< Spot > vertices = selectionModel.getSelectedVertices();
		if ( vertices.isEmpty() )
			vertices = model.getGraph().vertices();

		SortTree.sortCellLifetime( model, vertices );
		appModel.getBranchGraphSync().sync();
	}

	private void showLineageView() {
		if ( projectModel == null )
			return;
		final CompactLineageFrame frame =
				new CompactLineageFrame( projectModel );
		frame.setVisible(true);
	}

	private void filterOutIsolatedSpots()
	{
		commandService.run( FilterOutIsolatedSpots.class, true, "appModel", projectModel );
	}

	private void exportLengths()
	{
		commandService.run( LineageLengthExporter.class, true, "appModel", projectModel );
	}

	private void exportCountsPerLineage()
	{
		commandService.run( ExportCounts.class, true, "appModel", projectModel );
	}

	private void exportCountsPerTimepoint()
	{
		commandService.run( ExportSpotCountsPerTimepointCommand.class, true, "projectModel", projectModel,
				"context", projectModel.getContext() );
	}

	private void exportDivisionCountsPerTimepoint()
	{
		commandService.run( ExportDivisionCountsPerTimepointCommand.class, true, "projectModel", projectModel,
				"context", projectModel.getContext() );
	}

	private void showDivisionCountsOverTime()
	{
		commandService.run( ShowDivisionCountsOverTimeCommand.class, true, "projectModel", projectModel );
	}

	private void changeBranchLabels()
	{
		RenameBranchLabels.run( projectModel );
	}

	private void labelSpotsSystematically()
	{
		LabelSpotsSystematicallyDialog.showDialog( projectModel );
	}

	private void addCenterSpots()
	{
		AddCenterSpots.addSpots( projectModel );
	}

	private void mirrorSpots()
	{
		MirrorEmbryo.run( projectModel );
	}

	private void createConflictTagSet()
	{
		CreateConflictTagSetCommand.run( projectModel );
	}

	private void fuseSpots()
	{
		FuseSpots.run( projectModel );
	}

	private void locateTags()
	{
		LocateTagsFrame.run( projectModel );
	}

	private void runCellDivisionsTagSet()
	{
		commandService.run( CellDivisionsTagSetCommand.class, true, "projectModel", projectModel );
	}

	private void createDummyTagSet()
	{
		CreateDummyTagSet.createDummyTagSet( projectModel, commandService );
	}
}
