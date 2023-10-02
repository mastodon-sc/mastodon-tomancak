package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

public class MastodonGitNewBranch implements Command
{

	@Parameter
	private WindowManager windowManager;

	@Parameter( label = "Branch name", persist = false )
	private String branchName;

	@Override
	public void run()
	{
		try
		{
			MastodonGitRepository.createNewBranch( windowManager, branchName );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
