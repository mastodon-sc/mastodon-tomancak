package org.mastodon.mamut.tomancak.collaboration.commands;

import org.mastodon.mamut.tomancak.collaboration.MastodonGitSettingsService;
import org.scijava.Initializable;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, label = "Set Author Name", visible = false )
public class MastodonGitSetAuthorCommand implements Command, Initializable
{
	@Parameter
	private MastodonGitSettingsService settings;

	@Parameter( visibility = ItemVisibility.MESSAGE )
	private String description = "<html><body align=left>"
			+ "The name and email that you specify below<br>"
			+ "are used to identify you as the author of the<br>"
			+ "changes you make to the shared project.<br><br>"
			+ "Name and email are likely to become publicly visible on the internet.<br>"
			+ "You may use a nickname and dummy email address if you wish.";

	@Parameter( label = "Author name", persist = false )
	private String authorName;

	@Parameter( label = "Author email", persist = false )
	private String authorEmail = "noreply@example.com";

	@Override
	public void initialize()
	{
		authorName = settings.getAuthorName();
		authorEmail = settings.getAuthorEmail();
	}

	@Override
	public void run()
	{
		settings.setAuthorName( authorName );
		settings.setAuthorEmail( authorEmail );
	}
}
