package org.mastodon.mamut.tomancak.collaboration.commands;

import java.io.File;

import org.mastodon.mamut.tomancak.collaboration.ErrorDialog;
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
		label = "Mastodon Git - Download Shared Project (clone)",
		menuPath = "Plugins > Mastodon Git > Download Shared Project" )
public class MastodonGitCloneRepository extends AbstractCancellable implements Command
{
	@Parameter
	Context context;

	@Parameter( label = "URL on github or gitlab", description = URL_DESCRIPTION )
	String repositoryURL;

	private static final String URL_DESCRIPTION = "<html><body>"
			+ "Here are two examples of valid URLs:<br>"
			+ "<ul>"
			+ "<li>https://github.com/username/repositoryname.git</li>"
			+ "<li>git@github.com:username/repositoryname.git (if you use SSH to authenticate)</li>"
			+ "</ul>"
			+ "</body></html>";

	@Parameter( label = "Directory, to store the project:", style = "directory", description = DIRECTORY_DESCRIPTION )
	File directory;

	private static final String DIRECTORY_DESCRIPTION = "<html><body>"
			+ "A copy of the shared project will be downloaded to your computer.<br>"
			+ "Please select a directory where to store it.<br>"
			+ "The directory should be empty, or select \"Create new subdirectory\"."
			+ "</body></html>";

	@Parameter( label = "Create new subdirectory", required = false, description = CREATE_SUBDIRECTORY_DESCRIPTION )
	boolean createSubdirectory = false;

	private static final String CREATE_SUBDIRECTORY_DESCRIPTION = "<html><body>"
			+ "If selected, a new subdirectory will be created in the selected directory.<br>"
			+ "The name of the subdirectory will be the name of the repository."
			+ "</body></html>";

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
			ErrorDialog.showErrorMessage( "Download Shares Project (Clone)", e );
		}
	}

}
