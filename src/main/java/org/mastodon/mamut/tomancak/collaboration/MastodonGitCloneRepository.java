package org.mastodon.mamut.tomancak.collaboration;

import java.io.File;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

// TODOs:
// - warn if parentDirectory already exists
// - warn if repositoryName already exists and the corresponding directory is not empty
// - fill repositoryName with a default value based on the repositoryURL
public class MastodonGitCloneRepository implements Command
{
	@Parameter
	Context context;

	@Parameter( label = "URL on github or gitlab" )
	String repositoryURL;

	@Parameter( label = "Directory that will contain the cloned repository", style = "directory" )
	File parentDirectory;

	@Parameter( label = "Repository name" )
	String repositoryName;

	@Override
	public void run()
	{
		try
		{
			File directory = new File( this.parentDirectory, repositoryName );
			MastodonGitUtils.cloneRepository( repositoryURL, directory );
			MastodonGitUtils.openProjectInRepository( context, directory );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}
}
