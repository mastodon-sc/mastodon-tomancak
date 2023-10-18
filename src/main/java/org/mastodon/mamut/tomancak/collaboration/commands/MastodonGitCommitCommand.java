package org.mastodon.mamut.tomancak.collaboration.commands;

import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;

@Plugin( type = Command.class, label = "Add Save Point (commit)", visible = false )
public class MastodonGitCommitCommand extends AbstractCancellable implements Command
{
	@Parameter
	private MastodonGitRepository repository;

	@Parameter( label = "Save point message", style = "text area", persist = false,
			description = "A good message, is very helpful when inspecting the history of changes." )
	private String commitMessage;

	@Parameter( visibility = ItemVisibility.MESSAGE )
	private String comment = "Please describe briefly the changes since the last save point!";

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
