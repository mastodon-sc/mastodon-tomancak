package org.mastodon.mamut.tomancak.collaboration.sshauthentication;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;

/**
 * Example of how to use JGit with a custom SSH key.
 */
public class JGitSshAuthenticationExample
{
	public static void main( String... args ) throws Exception
	{
		String projectPath = "/home/arzt/devel/mastodon/mastodon/src/test/resources/org/mastodon/mamut/examples/tiny/tiny-project.mastodon";
		String repositoryName = "mgit-test";
		String repositoryURL = "git@github.com:masgitoff/mastodon-test-dataset.git";
		File parentDirectory = new File( "/home/arzt/tmp/" );

		SshSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder()
				.setPreferredAuthentications( "publickey" )
				.setHomeDirectory( new File( "/home/arzt/" ) )
				.setSshDirectory( new File( "/home/arzt/ssh-experiment" ) )
				.build( new JGitKeyCache() );
		try (Git git = Git.cloneRepository()
				.setURI( repositoryURL )
				.setDirectory( new File( parentDirectory, "xyz" ) )
				.setCredentialsProvider( new CustomCredentialsProvider() )
				.setTransportConfigCallback( transport -> ( ( SshTransport ) transport ).setSshSessionFactory( sshSessionFactory ) )
				.call())
		{
			git.push()
					.setTransportConfigCallback( transport -> ( ( SshTransport ) transport ).setSshSessionFactory( sshSessionFactory ) )
					.setCredentialsProvider( new CustomCredentialsProvider() )
					.call();
		}
	}
}
