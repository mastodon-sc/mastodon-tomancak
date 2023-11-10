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
import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCloneRepository;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCommitCommand;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitCreateRepository;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitNewBranch;
import org.mastodon.mamut.tomancak.collaboration.commands.MastodonGitSetAuthorCommand;
import org.mastodon.mamut.tomancak.collaboration.exceptions.GraphMergeConflictException;
import org.mastodon.mamut.tomancak.collaboration.exceptions.GraphMergeException;
import org.mastodon.mamut.tomancak.collaboration.utils.ActionDescriptions;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicDescriptionProvider;
import org.mastodon.mamut.tomancak.collaboration.utils.BasicMamutPlugin;
import org.mastodon.mamut.tomancak.collaboration.settings.MastodonGitSettingsService;
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
			"[mastodon git] clone",
			"Plugins > Git > Initialize > Download Shared Project (clone)",
			"Download a shared project, save a copy on the local disc and open it with Mastodon.",
			MastodonGitController::cloneGitRepository );

	private static final String SET_AUTHOR_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] set author",
			"Plugins > Git > Initialize > Set Author Name",
			"Set the author name that is used for your commits.",
			MastodonGitController::setAuthor );

	private static final String SYNCHRONIZE_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] synchronize",
			"Plugins > Git > Synchronize (commit, pull, push)",
			"Download remote changes and upload local changes.",
			MastodonGitController::synchronize );

	private static final String COMMIT_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] commit",
			"Plugins > Git > Add Save Point (commit)",
			"Commit changes to the git repository.",
			MastodonGitController::commit );

	private static final String PUSH_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] push",
			"Plugins > Git > Upload Changes (push)",
			"Push local changed to the remote server.",
			MastodonGitController::push );

	private static final String PULL_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] pull",
			"Plugins > Git > Download Changes (pull)",
			"Pull changes from the remote server and merge them with my changes.",
			MastodonGitController::pull );

	private static final String RESET_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] git reset",
			"Plugins > Git > Go Back To Latest Save Point (reset)",
			"Discard all changes made since the last save point.",
			MastodonGitController::reset );

	private static final String NEW_BRANCH_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] new branch",
			"Plugins > Git > Branches > Create New Branch",
			"Create a new branch in the git repository.",
			MastodonGitController::newBranch );

	private static final String SHOW_BRANCH_NAME_ACTION_KEY = actionDescriptions.addActionDescription(
			"[mastodon git] show branch name",
			"Plugins > Git > Branches > Show Branch Name",
			"Show the name of the current git branch",
			MastodonGitController::showBranchName );

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
		run( "Upload Changes (Push)", () -> {
			repository.push();
			NotificationDialog.show( "Upload Changes (Push)",
					"<html><body><font size=+4 color=green>&#10003</font> Completed successfully." );
		} );
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
			run( "Switch To Branch", () -> repository.switchBranch( selectedBranch ) );
		}
		catch ( Exception e )
		{
			ErrorDialog.showErrorMessage( "Select Branch", e );
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
			ErrorDialog.showErrorMessage( "Merge Branch", e );
		}
	}

	private void pull()
	{
		run( "Download Changes (Pull)", () -> {
			try
			{
				repository.pull();
			}
			catch ( GraphMergeException e )
			{
				if ( !( e instanceof GraphMergeConflictException ) )
					e.printStackTrace();
				SwingUtilities.invokeLater( () -> suggestPullAlternative( e.getMessage() ) );
			}
		} );
	}

	private void suggestPullAlternative( String errorMessage )
	{
		// TODO: add pull alternative, save to new branch
		String title = "Conflict During Download Of Changes (Pull)";
		String message = "There was a merge conflict during the pull. Details:\n"
				+ "  " + errorMessage + "\n\n"
				+ "You made changes on your computer that could not be automatically\n"
				+ "merged with the changes on the server.\n\n"
				+ "You can either:\n"
				+ "  1. Throw away your local changes & local save points.\n"
				+ "  2. Or cancel (And maybe save your local changes to a new branch,\n"
				+ "             which you can then be merged into the remote branch.)\n";

		String[] options = { "Discard Local Changes", "Cancel" };
		int result = JOptionPane.showOptionDialog( null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[ 0 ] );
		if ( result == JOptionPane.YES_OPTION )
			resetToRemoteBranch();
	}

	private void resetToRemoteBranch()
	{
		run( "Throw Away All Local Changes (Reset To Remote)", () -> repository.resetToRemoteBranch() );
	}

	private void reset()
	{
		run( "Go Back To Last Save Point (Reset)", () -> repository.reset() );
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

	private void showBranchName()
	{
		run( "Show Branch Name", () -> {
			String longBranchName = repository.getCurrentBranch();
			String shortBranchName = longBranchName.replaceAll( "^refs/heads/", "" );
			String title = "Current Branch Name";
			String message = "<html><body>The current branch is:<br><b>" + shortBranchName;
			SwingUtilities.invokeLater( () ->
					JOptionPane.showMessageDialog( null, message, title, JOptionPane.PLAIN_MESSAGE ) );
		} );
	}

	private void synchronize()
	{
		// check if clean
		// - yes, continue
		// - no -> ask for commit message, and commit

		// pull
		// try to merge
		// - no conflict -> continue
		// - conflict, let the user change an option:
		//    - discard local changes
		//    - cancel

		// push
		// notify about success or failure
		run( "Synchronize Changes", () -> {
			boolean clean = repository.isClean();
			if ( !clean )
			{
				String commitMessage = CommitMessageDialog.showDialog( "Add Save Point (commit)" );
				if ( commitMessage == null )
					return;
				repository.commitWithoutSave( commitMessage );
			}
			try
			{
				repository.pull();
			}
			catch ( GraphMergeException e )
			{
				if ( !( e instanceof GraphMergeConflictException ) )
					e.printStackTrace();
				SwingUtilities.invokeLater( () -> suggestPullAlternative( e.getMessage() ) );
				return;
			}
			repository.push();
			NotificationDialog.show( "Synchronize Changes (Commit, Pull, Push)",
					"<html><body><font size=+4 color=green>&#10003</font> Completed successfully." );
		} );
	}

	private void run( String title, RunnableWithException action )
	{
		new Thread( () -> {
			try
			{
				action.run();
			}
			catch ( CancellationException e )
			{
				// ignore
			}
			catch ( Exception e )
			{
				ErrorDialog.showErrorMessage( title, e );
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
