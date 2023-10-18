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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCloneRepository;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCommitCommand;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCreateRepository;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitNewBranch;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitSetAuthorCommand;
import org.mastodon.mamut.tomancak.collaboration.utils.ActionDescriptions;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicDescriptionProvider;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicMamutPlugin;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODO: disable commands if not in a git repo
@Plugin( type = MamutPlugin.class )
public class MastodonGitController extends BasicMamutPlugin
{
	@Parameter
	private CommandService commandService;

	@Parameter
	private MastodonGitSettingsService settingsService;

	public static final ActionDescriptions< MastodonGitController > actionDescriptions = new ActionDescriptions<>( MastodonGitController.class );

	private static final String SHARE_PROJECT_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] share project",
			"Plugins > Git > Initialize > Share Project",
			"Upload Mastodon project to a newly created git repository.",
			MastodonGitController::shareProject );

	private static final String CLONE_REPOSITORY_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] clone repository",
			"Plugins > Git > Initialize > Clone Existing Repository",
			"Clone a git repository to a new Mastodon project.",
			MastodonGitController::cloneGitRepository );

	private static final String SET_AUTHOR_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] set author",
			"Plugins > Git > Initialize > Set Author Name",
			"Set the author name that is used for your commits.",
			MastodonGitController::setAuthor );

	private static final String COMMIT_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] commit",
			"Plugins > Git > Add Save Point (commit)",
			"Commit changes to the git repository.",
			MastodonGitController::commit );

	private static final String PUSH_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] push",
			"Plugins > Git > Upload Changes (push)",
			"Push changes to the git repository.",
			MastodonGitController::push );

	private static final String PULL_ACTION_KEY = actionDescriptions.
			addActionDescription(
					"[mastodon git] pull",
					"Plugins > Git > Download Changes (pull)",
					"Pull changes from the git repository.",
					MastodonGitController::pull );

	private static final String RESET_ACTION_KEY = actionDescriptions.
			addActionDescription(
					"[mastodon git] git reset",
					"Plugins > Git > Undo Changes (reset)",
					"Reset changes in the git repository.",
					MastodonGitController::reset );

	private static final String NEW_BRANCH_ACTION_KEY = actionDescriptions.
			addActionDescription(
					"[mastodon git] new branch",
					"Plugins > Git > Branches > Create New Branch",
					"Create a new branch in the git repository.",
					MastodonGitController::newBranch );

	private static final String SWITCH_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] switch branch",
			"Plugins > Git > Branches > Switch Branch",
			"Switch to a different branch in the git repository.",
			MastodonGitController::switchBranch );

	private static final String MERGE_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] merge branch",
			"Plugins > Git > Branches > Merge Branch",
			"Merge a branch into the current branch.",
			MastodonGitController::mergeBranch );

	private static final List< String > IN_REPOSITORY_ACTIONS = Arrays.asList(
			COMMIT_ACTION_KEY,
			PUSH_ACTION_KEY,
			PULL_ACTION_KEY,
			RESET_ACTION_KEY,
			NEW_BRANCH_ACTION_KEY,
			SWITCH_ACTION_KEY,
			MERGE_ACTION_KEY );

	private MastodonGitRepository repository;

	public MastodonGitController()
	{
		super( actionDescriptions );
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		repository = new MastodonGitRepository( getWindowManager() );
		updateEnableCommands();
	}

	private void setAuthor()
	{
		commandService.run( MastodonGitSetAuthorCommand.class, true );
	}

	private void shareProject()
	{
		if ( !settingsService.isAuthorSpecified() )
		{
			askForAuthorName( "Please set your author name before sharing a project." );
			return;
		}
		MastodonGitCreateRepository.Callback callback = ( File directory, String url ) -> {
			this.repository = MastodonGitRepository.shareProject( getWindowManager(), directory, url );
			updateEnableCommands();
		};
		commandService.run( MastodonGitCreateRepository.class, true, "callback", callback );
	}

	private void updateEnableCommands()
	{
		boolean isRepository = repository.isRepository();
		IN_REPOSITORY_ACTIONS.forEach( action -> setActionEnabled( action, isRepository ) );
	}

	private void cloneGitRepository()
	{
		commandService.run( MastodonGitCloneRepository.class, true );
	}

	private void commit()
	{
		if ( !settingsService.isAuthorSpecified() )
		{
			askForAuthorName( "Please set your author name before your first commit." );
			return;
		}
		commandService.run( MastodonGitCommitCommand.class, true, "repository", repository );
	}

	private void push()
	{
		run( () -> repository.push() );
	}

	private void newBranch()
	{
		commandService.run( MastodonGitNewBranch.class, true, "repository", repository );
	}

	private void switchBranch()
	{
		try
		{
			// TODO: the branches are not formatted nicely
			String message = "Select a branch";
			try
			{
				repository.fetchAll();
			}
			catch ( Exception e )
			{
				message += " \n(There was a failure downloading the latest branch changes.)";
			}
			List< String > branches = repository.getBranches();
			String currentBranch = repository.getCurrentBranch();
			// show JOptionPane that allows to select a branch
			String selectedBranch = ( String ) JOptionPane.showInputDialog( null, message, "Switch Git Branch", JOptionPane.PLAIN_MESSAGE, null, branches.toArray(), currentBranch );
			if ( selectedBranch == null )
				return;
			// switch to selected branch
			run( () -> repository.switchBranch( selectedBranch ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void mergeBranch()
	{
		if ( !settingsService.isAuthorSpecified() )
		{
			askForAuthorName( "You need to set your author name before you can merge branches." );
			return;
		}
		try
		{
			List< String > branches = repository.getBranches();
			String currentBranch = repository.getCurrentBranch();
			branches.remove( currentBranch );
			// show JOptionPane that allows to select a branch
			String selectedBranch = ( String ) JOptionPane.showInputDialog( null, "Select a branch", "Switch Git Branch", JOptionPane.PLAIN_MESSAGE, null, branches.toArray(), null );
			if ( selectedBranch == null )
				return;
			repository.mergeBranch( selectedBranch );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void pull()
	{
		run( () -> {
			try
			{
				repository.pull();
			}
			catch ( MergeConflictDuringPullException e )
			{
				SwingUtilities.invokeLater( () -> suggestPullAlternative() );
			}
		} );
	}

	private void suggestPullAlternative()
	{
		// TODO: add pull alternative, save to new branch
		String title = "Conflict During Pull";
		String message = "There was a merge conflict during the pull.\n"
				+ "You made changes on your computer that conflict with changes on the server.\n"
				+ "The conflicts could not be resolved automatically.\n"
				+ "You can either:\n"
				+ "  1. Throw away you local changes.\n"
				+ "  2. Cancel (And maybe save your local changes to a new branch,\n"
				+ "             which you can then merge into the remote branch.)\n";

		String[] options = { "Discard Local Changes", "Cancel" };
		int result = JOptionPane.showOptionDialog( null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[ 0 ] );
		if ( result == JOptionPane.YES_OPTION )
			resetToRemoteBranch();
	}

	private void resetToRemoteBranch()
	{
		run( () -> repository.resetToRemoteBranch() );
	}

	private void reset()
	{
		run( () -> repository.reset() );
	}

	private void askForAuthorName( String message )
	{
		String title = "Set Author Name";
		String[] options = { "Set Author Name", "Cancel" };
		int result = JOptionPane.showOptionDialog( null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[ 0 ] );
		if ( result == JOptionPane.YES_OPTION )
			setAuthor();
	}

	private void run( RunnableWithException action )
	{
		new Thread( () -> {
			try
			{
				action.run();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		} ).start();
	}

	interface RunnableWithException
	{
		void run() throws Exception;
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
