package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.tomancak.collaboration.MastodonGitUtils;
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
		MastodonGitUtils.createNewBranch( windowManager, branchName );
	}
}
