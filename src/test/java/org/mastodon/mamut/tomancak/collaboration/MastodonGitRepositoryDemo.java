package org.mastodon.mamut.tomancak.collaboration;

import java.io.File;

import org.scijava.Context;

public class MastodonGitRepositoryDemo
{
	public static void main( String... args ) throws Exception
	{
		String projectPath = "/home/arzt/devel/mastodon/mastodon/src/test/resources/org/mastodon/mamut/examples/tiny/tiny-project.mastodon";
		String repositoryName = "mgit-test";
		String repositoryURL = "git@github.com:maarzt/mgit-test.git";
		File parentDirectory = new File( "/home/arzt/tmp/" );

//		Context context = new Context();
//		WindowManager windowManager = new WindowManager( context );
//		windowManager.getProjectManager().open( new MamutProjectIO().load( projectPath ) );
//		MastodonGitUtils.createRepositoryAndUpload( windowManager, parentDirectory, repositoryName, repositoryURL );

//		MastodonGitUtils.cloneRepository( repositoryURL, new File( parentDirectory, "2/" ) );

		MastodonGitRepository.openProjectInRepository( new Context(), new File( parentDirectory, "2/" ) );
	}
}
