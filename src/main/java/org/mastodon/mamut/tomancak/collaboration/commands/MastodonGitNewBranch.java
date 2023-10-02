package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

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
