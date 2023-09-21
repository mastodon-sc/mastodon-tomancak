/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.tomancak.collaboration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MastodonGitUtils
{

	public static void main( String... args ) throws IOException, SpimDataException
	{
		String projectPath = "/home/arzt/devel/mastodon/mastodon/src/test/resources/org/mastodon/mamut/examples/tiny/tiny-project.mastodon";
		Context context = new Context();
		WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( new MamutProjectIO().load( projectPath ) );
		File parentDirectory = new File( "/home/arzt/tmp/" );
		String repositoryName = "mgit-test";
		String repositoryURL = "git@github.com:maarzt/mgit-test.git";
		MastodonGitUtils.createRepositoryAndUpload( windowManager, parentDirectory, repositoryName, repositoryURL );
	}

	public static void createRepositoryAndUpload( WindowManager windowManager, File parentDirectory, String repositoryName, String repositoryURL )
	{
		try
		{
			Path gitRepositoryPath = parentDirectory.toPath().resolve( repositoryName );
			Files.createDirectories( gitRepositoryPath );
			Git git = Git.init().setDirectory( gitRepositoryPath.toFile() ).call();
			Path mastodonProjectPath = gitRepositoryPath.resolve( "mastodon.project" );
			Files.createDirectory( mastodonProjectPath );
			windowManager.getProjectManager().saveProject( mastodonProjectPath.toFile() );
			git.add().addFilepattern( "mastodon.project" ).call();
			git.commit().setMessage( "Initial commit" ).call();
			git.remoteAdd().setName( "origin" ).setUri( new URIish( repositoryURL ) ).call();
			git.push().setRemote( "origin" ).call();
			git.close();
		}
		catch ( GitAPIException | IOException | URISyntaxException e )
		{
			throw new RuntimeException( e );
		}
	}
}
