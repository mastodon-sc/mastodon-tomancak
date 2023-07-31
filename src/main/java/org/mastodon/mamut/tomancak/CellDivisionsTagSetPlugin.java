package org.mastodon.mamut.tomancak;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * A Mastodon plugin that adds a tag set for highlighting cell divisions.
 * <p>
 * This class executes the {@link CellDivisionsTagSetCommand} when the
 * menu item is clicked.
 */
@Plugin( type = MamutPlugin.class )
public class CellDivisionsTagSetPlugin implements MamutPlugin
{
	@Parameter
	private CommandService commandService;

	private static final String ID = "[tomancak] create cell divisions tag set";

	private static final String[] KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts = Collections.singletonMap( ID, "Add Tag Set to Highlight Cell Divisions ..." );

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
			descriptions.add( ID, KEYS, "Adds a tag set to highlight cell divisions." );
		}
	}

	private final AbstractNamedAction action;

	public CellDivisionsTagSetPlugin()
	{
		action = new RunnableAction( ID, () -> {
			if ( pluginAppModel != null )
				run();
		} );
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
		action.setEnabled( appModel != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( ID ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( action, KEYS );
	}

	private void run()
	{
		commandService.run( CellDivisionsTagSetCommand.class, true, "appModel", pluginAppModel.getAppModel() );
	}
}
