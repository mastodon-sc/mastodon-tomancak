package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.tomancak.collaboration.MastodonGitUtils;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

// TODOs:
// - allow to ignore project.xml and gui.xml
// - allow to provide a commit message
public class MastodonGitCommit implements Command
{
	@Parameter
	WindowManager windowManager;

	@Override
	public void run()
	{
		MastodonGitUtils.commit( windowManager );
	}
}
