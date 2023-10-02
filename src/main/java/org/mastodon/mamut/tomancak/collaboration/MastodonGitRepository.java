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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.mamut.tomancak.collaboration.credentials.PersistentCredentials;
import org.mastodon.mamut.tomancak.merging.Dataset;
import org.mastodon.mamut.tomancak.merging.MergeDatasets;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

// make it one synchronized class per repository
// don't allow to open a repository twice (maybe read only)
public class MastodonGitRepository
{

	private static final PersistentCredentials credentials = new PersistentCredentials();

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

	public static void createRepositoryAndUpload(
			WindowManager windowManager,
			File parentDirectory,
			String repositoryName,
			String repositoryURL )
			throws Exception
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
		git.push().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).setRemote( "origin" ).call();
		git.close();
	}

	public static void cloneRepository( String repositoryURL, File parentDirectory ) throws Exception
	{
		Git git = Git.cloneRepository()
				.setURI( repositoryURL )
				.setCredentialsProvider( credentials.getSingleUseCredentialsProvider() )
				.setDirectory( parentDirectory )
				.call();
		git.close();
	}

	public static void openProjectInRepository( Context context, File directory ) throws Exception
	{
		WindowManager windowManager = new WindowManager( context );
		Path path = directory.toPath().resolve( "mastodon.project" );
		windowManager.getProjectManager().openWithDialog( new MamutProjectIO().load( path.toAbsolutePath().toString() ) );
		new MainWindow( windowManager ).setVisible( true );
	}

	public static void commit( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			windowManager.getProjectManager().saveProject();
			List< DiffEntry > changedFiles = relevantChanges( git );
			if ( !changedFiles.isEmpty() )
			{
				for ( DiffEntry diffEntry : changedFiles )
				{
					git.add().addFilepattern( diffEntry.getOldPath() ).call();
					git.add().addFilepattern( diffEntry.getNewPath() ).call();
				}
				git.commit().setMessage( "Commit from Mastodon" ).call();
			}
		}
	}

	private static List< DiffEntry > relevantChanges( Git git ) throws GitAPIException
	{
		return git.diff().setPathFilter( relevantFilesFilter() ).call();
	}

	public static void push( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			git.push().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).setRemote( "origin" ).call();
		}
	}

	public static void createNewBranch( WindowManager windowManager, String branchName ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			git.checkout().setCreateBranch( true ).setName( branchName ).call();
		}
	}

	public static void switchBranch( WindowManager windowManager, String branchName ) throws Exception
	{
		File projectRoot = windowManager.getProjectManager().getProject().getProjectRoot();
		try (Git git = initGit( projectRoot ))
		{
			boolean isRemoteBranch = branchName.startsWith( "refs/remotes/" );
			if ( isRemoteBranch )
			{
				String simpleName = getSimpleName( branchName );
				boolean conflict = git.branchList().call().stream().map( Ref::getName ).anyMatch( localName -> simpleName.equals( getSimpleName( localName ) ) );
				if ( conflict )
					throw new RuntimeException( "There's already a local branch with the same name." );
				git.checkout()
						.setCreateBranch( true )
						.setName( simpleName )
						.setUpstreamMode( CreateBranchCommand.SetupUpstreamMode.TRACK )
						.setStartPoint( branchName )
						.call();
			}
			else
				git.checkout().setName( branchName ).call();
		}
		windowManager.getProjectManager().openWithDialog( new MamutProjectIO().load( projectRoot.getAbsolutePath() ) );
	}

	private static String getSimpleName( String branchName )
	{
		String[] parts = branchName.split( "/" );
		return parts[ parts.length - 1 ];
	}

	public static List< String > getBranches( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			return git.branchList().setListMode( ListBranchCommand.ListMode.ALL ).call().stream().map( Ref::getName ).collect( Collectors.toList() );
		}
	}

	public static String getCurrentBranch( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			return git.getRepository().getFullBranch();
		}
	}

	public static void mergeBranch( WindowManager windowManager, String selectedBranch ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			windowManager.getProjectManager().saveProject();
			boolean clean = isClean( git );
			if ( !clean )
				throw new RuntimeException( "There are uncommitted changes. Please commit or stash them before merging." );
			File projectRoot = windowManager.getProjectManager().getProject().getProjectRoot();
			String currentBranch = getCurrentBranch( windowManager );
			Dataset dsA = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setName( selectedBranch ).call();
			Dataset dsB = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setName( currentBranch ).call();
			git.merge().setCommit( false ).include( git.getRepository().exactRef( selectedBranch ) ).call(); // TODO selected branch, should not be a string but a ref instead
			windowManager.getProjectManager().open( new MamutProject( null, dsA.project().getDatasetXmlFile() ) );
			final MergeDatasets.OutputDataSet output = new MergeDatasets.OutputDataSet( windowManager.getAppModel().getModel() );
			double distCutoff = 1000;
			double mahalanobisDistCutoff = 1;
			double ratioThreshold = 2;
			MergeDatasets.merge( dsA, dsB, output, distCutoff, mahalanobisDistCutoff, ratioThreshold );
			windowManager.getProjectManager().saveProject( projectRoot );
			commit( windowManager );
		}
	}

	public static void pull( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			windowManager.getProjectManager().saveProject();
			boolean isClean = isClean( git );
			if ( !isClean )
				throw new RuntimeException( "There are uncommitted changes. Please commit or stash them before.setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ) pulling." );
			git.fetch().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).call();
			int aheadCount = BranchTrackingStatus.of( git.getRepository(), git.getRepository().getBranch() ).getAheadCount();
			if ( aheadCount > 0 )
				throw new RuntimeException( "There are local changes. UNSUPPORTED operation. Cannot be done without merge." );
			git.pull().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).call();
			reloadFromDisc( windowManager );
		}
	}

	private static void reloadFromDisc( WindowManager windowManager ) throws IOException, SpimDataException
	{
		File projectRoot = windowManager.getProjectManager().getProject().getProjectRoot();
		windowManager.getProjectManager().openWithDialog( new MamutProjectIO().load( projectRoot.getAbsolutePath() ) );
	}

	public static void reset( WindowManager windowManager ) throws Exception
	{
		try (Git git = initGit( windowManager ))
		{
			resetRelevantChanges( git );
			reloadFromDisc( windowManager );
		}
	}

	private static void resetRelevantChanges( Git git ) throws GitAPIException
	{
		// NB: More complicated than a simple reset --hard, because gui.xml, project.xml and dataset.xml.backup should remain untouched.
		List< DiffEntry > diffEntries = relevantChanges( git );
		ResetCommand resetCommand = git.reset().setMode( ResetCommand.ResetType.HARD );
		for ( DiffEntry entry : diffEntries )
		{
			switch ( entry.getChangeType() )
			{
			case ADD:
				resetCommand.addPath( entry.getNewPath() );
				break;
			case DELETE:
				resetCommand.addPath( entry.getOldPath() );
				break;
			case MODIFY:
				resetCommand.addPath( entry.getNewPath() );
				resetCommand.addPath( entry.getOldPath() );
				break;
			}
			resetCommand.call();
		}
	}

	private static Git initGit( WindowManager windowManager ) throws IOException
	{
		File projectRoot = windowManager.getProjectManager().getProject().getProjectRoot();
		return initGit( projectRoot );
	}

	private static Git initGit( File projectRoot ) throws IOException
	{
		boolean correctFolder = projectRoot.getName().equals( "mastodon.project" );
		if ( !correctFolder )
			throw new RuntimeException( "The current project does not appear to be in a git repo." );
		File gitRoot = projectRoot.getParentFile();
		if ( !new File( gitRoot, ".git" ).exists() )
			throw new RuntimeException( "The current project does not appear to be in a git repo." );
		return Git.open( gitRoot );
	}

	private static boolean isClean( Git git ) throws GitAPIException
	{
		return git.diff().setPathFilter( relevantFilesFilter() ).call().isEmpty();
	}

	private static TreeFilter relevantFilesFilter()
	{
		TreeFilter[] filters = {
				// only files in subdirectory "mastodon.project"
				PathFilter.create( "mastodon.project" ),
				// but exclude gui.xml project.xml and dataset.xml.backup
				PathFilter.create( "mastodon.project/gui.xml" ).negate(),
				PathFilter.create( "mastodon.project/project.xml" ).negate(),
				PathFilter.create( "mastodon.project/dataset.xml.backup" ).negate()
		};
		return AndTreeFilter.create( filters );
	}
}