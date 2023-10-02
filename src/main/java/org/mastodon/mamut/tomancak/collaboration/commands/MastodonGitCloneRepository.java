package org.mastodon.mamut.tomancak.collaboration.commands;

import java.io.File;

import org.mastodon.mamut.tomancak.collaboration.MastodonGitRepository;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODOs:
// - warn if parentDirectory already exists
// - warn if repositoryName already exists and the corresponding directory is not empty
// - fill repositoryName with a default value based on the repositoryURL
@Plugin( type = Command.class,
		label = "Mastodon Collaborative - Download Shared Project (clone)",
		menuPath = "Plugins > Mastodon Collaborative > Download Shared Project" )
public class MastodonGitCloneRepository implements Command
{
	@Parameter
	Context context;

	@Parameter( label = "URL on github or gitlab" )
	String repositoryURL;

	@Parameter( label = "Directory that will contain the cloned repository", style = "directory" )
	File directory;

	@Parameter( label = "Create new subdirectory", required = false )
	boolean createSubdirectory = false;

	@Override
	public void run()
	{
		try
		{
			directory = NewDirectoryUtils.createRepositoryDirectory( createSubdirectory, directory, repositoryURL );
			MastodonGitRepository.cloneRepository( repositoryURL, directory );
			MastodonGitRepository.openProjectInRepository( context, directory );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

}
