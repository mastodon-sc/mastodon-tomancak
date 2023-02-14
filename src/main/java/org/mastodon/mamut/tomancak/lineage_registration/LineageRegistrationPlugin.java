package org.mastodon.mamut.tomancak.lineage_registration;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * Shows the {@link LineageRegistrationDialog} and
 * executes the {@link LineageRegistrationAlgorithm} when
 * ok is clicked.
 */
@Plugin( type = MamutPlugin.class )
public class LineageRegistrationPlugin implements MamutPlugin
{
	private static final String MATCH_TREE = "[tomancak] match tree to other project";

	private static final String[] MATCH_TREE_KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts =
			Collections.singletonMap( MATCH_TREE, "Sort Lineage Tree (To Match Other Project)" );

	private MamutPluginAppModel pluginAppModel = null;

	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( CommandDescriptions descriptions )
		{
			descriptions.add( MATCH_TREE, MATCH_TREE_KEYS, "Sort the TrackScheme such that the order matches another project." );
		}
	}

	private final AbstractNamedAction matchTreeAction;

	public LineageRegistrationPlugin()
	{
		matchTreeAction = new RunnableAction( MATCH_TREE, this::matchTree );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		matchTreeAction.setEnabled( appModel != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", menu( "Trees Management", item( MATCH_TREE ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( matchTreeAction, MATCH_TREE_KEYS );
	}

	private void matchTree()
	{
		if ( pluginAppModel != null )
			showDialog( pluginAppModel.getAppModel() );
	}

	public static void showDialog( MamutAppModel appModel )
	{
		File otherProject = LineageRegistrationDialog.showDialog();
		if ( otherProject == null )
			return;
		Model model = openModel( otherProject );
		LineageRegistrationAlgorithm.run( model, appModel.getModel() );
	}

	private static Model openModel( File file )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( file.getAbsolutePath() );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			try ( final MamutProject.ProjectReader reader = project.openForReading() )
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
