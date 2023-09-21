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
package org.mastodon.mamut.tomancak.collaboration;

import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCloneRepository;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCommit;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCreateRepository;
import org.mastodon.mamut.tomancak.collaboration.utils.ActionDescriptions;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicDescriptionProvider;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicMamutPlugin;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutPlugin.class )
public class MastodonGitPlugins extends BasicMamutPlugin
{
	@Parameter
	private CommandService commandService;

	public static final ActionDescriptions< MastodonGitPlugins > actionDescriptions = new ActionDescriptions<>( MastodonGitPlugins.class )
			.addActionDescription( "[mastodon git] create repository",
					"Plugins > Git > Create Repository",
					"Upload Mastodon project to a newly created git repository.",
					MastodonGitPlugins::createRepository )
			.addActionDescription( "[mastodon git] clone repository",
					"Plugins > Git > Clone Repository",
					"Clone a git repository to a new Mastodon project.",
					MastodonGitPlugins::cloneGitRepository )
			.addActionDescription( "[mastodon git] commit",
					"Plugins > Git > Commit",
					"Commit changes to the git repository.",
					MastodonGitPlugins::commit );

	public MastodonGitPlugins()
	{
		super( actionDescriptions );
	}

	private void createRepository()
	{
		commandService.run( MastodonGitCreateRepository.class, true, "windowManager", getWindowManager() );
	}

	private void cloneGitRepository()
	{
		commandService.run( MastodonGitCloneRepository.class, true );
	}

	private void commit()
	{
		commandService.run( MastodonGitCommit.class, true, "windowManager", getWindowManager() );
	}

	@Plugin( type = CommandDescriptionProvider.class )
	public static class DescriptionProvider extends BasicDescriptionProvider
	{
		public DescriptionProvider()
		{
			super( actionDescriptions, KeyConfigContexts.MASTODON, KeyConfigContexts.TRACKSCHEME );
		}
	}
}
