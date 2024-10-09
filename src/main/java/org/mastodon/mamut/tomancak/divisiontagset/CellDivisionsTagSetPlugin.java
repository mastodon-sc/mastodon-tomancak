/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.divisiontagset;

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
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
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

	private static final Map< String, String > menuTexts = Collections.singletonMap( ID, "Add tag set to highlight cell divisions" );

	private ProjectModel projectModel = null;

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
			descriptions.add( ID, KEYS, "Adds a tag set to highlight cell divisions." );
		}
	}

	private final AbstractNamedAction action;

	public CellDivisionsTagSetPlugin()
	{
		action = new RunnableAction( ID, () -> {
			if ( projectModel != null )
				run();
		} );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( ProjectModel projectModel )
	{
		this.projectModel = projectModel;
		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		action.setEnabled( projectModel != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", menu( "Tags", item( ID ) ) ) );
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
		commandService.run( CellDivisionsTagSetCommand.class, true, "projectModel", projectModel );
	}
}
