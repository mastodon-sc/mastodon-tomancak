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
	private static final String EXPORT_PHYLOXML = "[tomancak] export phyloxml for selection";
	private static final String FLIP_DESCENDANTS = "[tomancak] flip descendants";
	private static final String COPY_TAG = "[tomancak] copy tag";
	private static final String INTERPOLATE_SPOTS = "[tomancak] interpolate spots";
	private static final String TWEAK_DATASET_PATH = "[tomancak] tweak dataset path";
	private static final String LABEL_SELECTED_SPOTS = "[tomancak] label spots";
	private static final String COMPACT_LINEAGE_VIEW = "[tomancak] lineage tree view";

	private static final String[] EXPORT_PHYLOXML_KEYS = { "not mapped" };
	private static final String[] FLIP_DESCENDANTS_KEYS = { "not mapped" };
	private static final String[] COPY_TAG_KEYS = { "not mapped" };
	private static final String[] INTERPOLATE_SPOTS_KEYS = { "not mapped" };
	private static final String[] TWEAK_DATASET_PATH_KEYS = { "not mapped" };
	private static final String[] LABEL_SELECTED_SPOTS_KEYS = { "not mapped" };
	private static final String[] COMPACT_LINEAGE_VIEW_KEYS = { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( EXPORT_PHYLOXML, "Export phyloXML for selection" );
		menuTexts.put( FLIP_DESCENDANTS, "Flip descendants" );
		menuTexts.put( COPY_TAG, "Copy Tag..." );
		menuTexts.put( INTERPOLATE_SPOTS, "Interpolate missing spots" );
		menuTexts.put( TWEAK_DATASET_PATH, "Edit BDV XML path..." );
		menuTexts.put( LABEL_SELECTED_SPOTS, "Label selected spots..." );
		menuTexts.put( COMPACT_LINEAGE_VIEW, "Show compact lineage");
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
			descriptions.add( FLIP_DESCENDANTS, FLIP_DESCENDANTS_KEYS, "Flip children in trackscheme graph." );
			descriptions.add( COPY_TAG, COPY_TAG_KEYS, "Copy tags: everything that has tag A assigned gets B assigned." );
			descriptions.add( INTERPOLATE_SPOTS, INTERPOLATE_SPOTS_KEYS, "Interpolate missing spots." );
			descriptions.add( TWEAK_DATASET_PATH, TWEAK_DATASET_PATH_KEYS, "Set the path to the BDV data and whether it is relative or absolute." );
			descriptions.add( LABEL_SELECTED_SPOTS, LABEL_SELECTED_SPOTS_KEYS, "Set label for all selected spots." );
			descriptions.add( COMPACT_LINEAGE_VIEW, COMPACT_LINEAGE_VIEW_KEYS, "Show compact representation of the lineage tree.");
		}
	}

	private final AbstractNamedAction exportPhyloXmlAction;

	private final AbstractNamedAction flipDescendantsAction;

	private final AbstractNamedAction copyTagAction;

	private final AbstractNamedAction interpolateSpotsAction;

	private final AbstractNamedAction tweakDatasetPathAction;

	private final AbstractNamedAction labelSelectedSpotsAction;

	private final AbstractNamedAction lineageTreeViewAction;

	private MamutPluginAppModel pluginAppModel;

	public TomancakPlugins()
	{
		exportPhyloXmlAction = new RunnableAction( EXPORT_PHYLOXML, this::exportPhyloXml );
		flipDescendantsAction = new RunnableAction( FLIP_DESCENDANTS, this::flipDescendants );
		copyTagAction = new RunnableAction( COPY_TAG, this::copyTag );
		interpolateSpotsAction = new RunnableAction( INTERPOLATE_SPOTS, this::interpolateSpots );
		tweakDatasetPathAction = new RunnableAction( TWEAK_DATASET_PATH, this::tweakDatasetPath );
		labelSelectedSpotsAction = new RunnableAction( LABEL_SELECTED_SPOTS, this::labelSelectedSpots );
		lineageTreeViewAction = new RunnableAction( COMPACT_LINEAGE_VIEW, this::showLineageView );
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
								item( EXPORT_PHYLOXML ),
								item( FLIP_DESCENDANTS ),
								item( INTERPOLATE_SPOTS ),
								item( LABEL_SELECTED_SPOTS ),
								item( COPY_TAG ),
								item( TWEAK_DATASET_PATH ),
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
		actions.namedAction( tweakDatasetPathAction, TWEAK_DATASET_PATH_KEYS );
		actions.namedAction( labelSelectedSpotsAction, LABEL_SELECTED_SPOTS_KEYS );
		actions.namedAction( lineageTreeViewAction, COMPACT_LINEAGE_VIEW_KEYS );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		exportPhyloXmlAction.setEnabled( appModel != null );
		flipDescendantsAction.setEnabled( appModel != null );
		copyTagAction.setEnabled( appModel != null );
		interpolateSpotsAction.setEnabled( appModel != null );
		tweakDatasetPathAction.setEnabled( appModel != null );
		labelSelectedSpotsAction.setEnabled( appModel != null );
		lineageTreeViewAction.setEnabled( appModel != null );
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

	private void tweakDatasetPath()
	{
		if ( pluginAppModel != null )
		{
			final MamutProject project = pluginAppModel.getWindowManager().getProjectManager().getProject();
			new DatasetPathDialog( null, project ).setVisible( true );
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

	private void showLineageView() {
		if( pluginAppModel == null )
			return;
		CompactLineageFrame frame =
			new CompactLineageFrame(pluginAppModel.getAppModel());
		frame.setVisible(true);
	}

}
