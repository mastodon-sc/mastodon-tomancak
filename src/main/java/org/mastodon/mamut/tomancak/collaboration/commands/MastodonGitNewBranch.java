package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, label = "Create New Branch", visible = false )
public class MastodonGitNewBranch implements Command
{

	@Parameter
	private MastodonGitRepository repository;

	@Parameter( label = "Branch name", persist = false )
	private String branchName;

	@Override
	public void run()
	{
		try
		{
			repository.createNewBranch( branchName );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
