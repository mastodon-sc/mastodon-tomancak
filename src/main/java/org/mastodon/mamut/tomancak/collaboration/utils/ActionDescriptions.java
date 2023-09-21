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
import java.util.List;
import java.util.function.Consumer;

/**
 * This class contains a list of details about actions that a plugin can
 * perform. It should be used together with {@link BasicMamutPlugin} and
 * {@link BasicDescriptionProvider}.
 */
public class ActionDescriptions< T >
{

	private final Class< T > pluginClass;

	private final List< Entry< ? > > entries = new ArrayList<>();

	public ActionDescriptions( Class< T > pluginClass )
	{
		this.pluginClass = pluginClass;
	}

	public final ActionDescriptions< T > addActionDescription( String key, String menuText, String description, Consumer< T > action )
	{
		return addActionDescription( key, menuText, description, action, "not mapped" );
	}

	public final ActionDescriptions< T > addActionDescription( String key, String menuText, String description, Consumer< T > action, String... keyStrokes )
	{
		entries.add( new Entry<>( key, menuText, keyStrokes, description, action ) );
		return this;
	}

	public Class< T > getPluginClass()
	{
		return pluginClass;
	}

	public List< Entry< ? > > getEntries()
	{
		return entries;
	}

	public static class Entry< T >
	{
		public final String key;

		public final String menuEntry;

		public final String[] shortCuts;

		public final String description;

		public final Consumer< T > action;

		public Entry( String key, String menuEntry, String[] shortCuts, String description, Consumer< T > action )
		{
			this.key = key;
			this.menuEntry = menuEntry;
			this.shortCuts = shortCuts;
			this.description = description;
			this.action = action;
		}
	}
}
