package org.mastodon.mamut.tomancak.collaboration.commands;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;

@Plugin( type = Command.class, label = "Add Save Point (commit)", visible = false )
public class MastodonGitCommitCommand extends AbstractCancellable implements Command
{
	@Parameter
	private MastodonGitRepository repository;

	@Parameter( label = "Commit message", style = "text area", persist = false, description = "A short description of the changes." )
	private String commitMessage;

	@Override
	public void run()
	{
		try
		{
			repository.commit( commitMessage );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
