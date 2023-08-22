package org.mastodon.mamut.tomancak.lineage_registration;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * A plugin that registers the cell lineages of two stereotypically developing embryos.
 * <p>
 * The plugin interacts with the {@link LineageRegistrationControlService} to
 * register and unregister the {@link MamutAppModel}
 * and to show the {@link LineageRegistrationFrame}.
 */
@Plugin( type = MamutPlugin.class )
public class LineageRegistrationPlugin implements MamutPlugin
{

	@Parameter
	LineageRegistrationControlService lineageRegistrationControlService;

	private static final String MATCH_TREE = "[tomancak] match tree to other project";

	private static final String[] MATCH_TREE_KEYS = { "not mapped" };

	private static final Map< String, String > menuTexts =
			Collections.singletonMap( MATCH_TREE, "Lineage Registration" );

	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( CommandDescriptions descriptions )
		{
			descriptions.add( MATCH_TREE, MATCH_TREE_KEYS,
					"Register the cell lineages of two stereotypically developing embryos, by analyzing cell division directions." );
		}
	}

	private final AbstractNamedAction matchTreeAction;

	public LineageRegistrationPlugin()
	{
		matchTreeAction = new RunnableAction( MATCH_TREE, this::matchTree );
	}

	@Override
	public void setAppPluginModel( ProjectModel model )
	{
		lineageRegistrationControlService.registerMastodonInstance( model );
		model.projectClosedListeners().add( () -> lineageRegistrationControlService.unregisterMastodonInstance( model ) );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( MATCH_TREE ) ) );
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
		lineageRegistrationControlService.showDialog();
	}
}
