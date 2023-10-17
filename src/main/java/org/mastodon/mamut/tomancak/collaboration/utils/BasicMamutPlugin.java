/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.tomancak.collaboration.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.util.Cast;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.tomancak.collaboration.MastodonGitController;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

/**
 * A class that simplifies the creation of a {@link MamutPlugin}.
 * See {@link MastodonGitController} for
 * usage example.
 */
public class BasicMamutPlugin implements MamutPlugin
{

	private final List< ActionDescriptions.Entry< ? > > actionDescriptions;

	private final Map< String, String > menuTexts = new HashMap<>();

	private final Map< String, AbstractNamedAction > actions = new HashMap<>();

	private final List< ViewMenuBuilder.MenuItem > menuItems = new ArrayList<>();

	private MamutAppModel appModel;

	private WindowManager windowManager;

	public < T > BasicMamutPlugin( ActionDescriptions< T > description )
	{
		if ( !this.getClass().equals( description.getPluginClass() ) )
			throw new IllegalArgumentException( "Plugin class mismatch." );
		actionDescriptions = description.getEntries();
		for ( ActionDescriptions.Entry< ? > entry : actionDescriptions )
		{
			menuTexts.put( entry.key, extractMenuText( entry.menuEntry ) );
			menuItems.add( initMenuItem( entry.key, entry.menuEntry ) );
			actions.put( entry.key, new RunnableAction( entry.key, () -> entry.action.accept( Cast.unchecked( this ) ) ) );
		}
	}

	public void setActionEnabled( String key, boolean enabled )
	{
		actions.get( key ).setEnabled( enabled );
	}

	private String extractMenuText( String menuEntry )
	{
		// From menuEntry, extract the last part, which is the menu item name.
		final String[] parts = menuEntry.split( ">" );
		return parts[ parts.length - 1 ].trim();
	}

	private ViewMenuBuilder.MenuItem initMenuItem( String key, String menuEntry )
	{
		final String[] parts = menuEntry.split( ">" );
		ViewMenuBuilder.MenuItem item = ViewMenuBuilder.item( key );
		for ( int i = parts.length - 2; i >= 0; i-- )
			item = ViewMenuBuilder.menu( parts[ i ].trim(), item );
		return item;
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel appPluginModel )
	{
		appModel = appPluginModel.getAppModel();
		windowManager = appPluginModel.getWindowManager();
		actions.forEach( ( key, action ) -> action.setEnabled( appModel != null ) );
		initialize();
	}

	protected void initialize() {}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return menuItems;
	}

	@Override
	public void installGlobalActions( Actions pluginActions )
	{
		for ( ActionDescriptions.Entry< ? > entry : actionDescriptions )
			pluginActions.namedAction( actions.get( entry.key ), entry.shortCuts );
	}

	protected MamutAppModel getAppModel()
	{
		return appModel;
	}

	protected WindowManager getWindowManager()
	{
		return windowManager;
	}
}
