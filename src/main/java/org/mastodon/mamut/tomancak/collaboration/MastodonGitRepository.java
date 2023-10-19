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
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.mastodon.graph.io.RawGraphIO;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.mamut.tomancak.collaboration.credentials.PersistentCredentials;
import org.mastodon.mamut.tomancak.collaboration.exceptions.GraphMergeConflictException;
import org.mastodon.mamut.tomancak.collaboration.exceptions.GraphMergeException;
import org.mastodon.mamut.tomancak.collaboration.utils.ConflictUtils;
import org.mastodon.mamut.tomancak.merging.Dataset;
import org.mastodon.mamut.tomancak.merging.MergeDatasets;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

// make it one synchronized class per repository
// don't allow to open a repository twice (maybe read only)
public class MastodonGitRepository
{

	private static final PersistentCredentials credentials = new PersistentCredentials();

	private final WindowManager windowManager;

	private final MastodonGitSettingsService settingsService;

	public MastodonGitRepository( WindowManager windowManager )
	{
		this.windowManager = windowManager;
		settingsService = windowManager.getContext().service( MastodonGitSettingsService.class );
	}

	public static MastodonGitRepository shareProject(
			WindowManager windowManager,
			File directory,
			String repositoryURL )
			throws Exception
	{
		if ( !directory.isDirectory() )
			throw new IllegalArgumentException( "Not a directory: " + directory );
		if ( !isEmpty( directory ) )
			throw new IllegalArgumentException( "Directory not empty: " + directory );
		Git git = Git.cloneRepository()
				.setURI( repositoryURL )
				.setCredentialsProvider( credentials.getSingleUseCredentialsProvider() )
				.setDirectory( directory )
				.call();
		Path mastodonProjectPath = directory.toPath().resolve( "mastodon.project" );
		if ( Files.exists( mastodonProjectPath ) )
			throw new RuntimeException( "The repository already contains a shared mastodon project: " + repositoryURL );
		Files.createDirectory( mastodonProjectPath );
		windowManager.getProjectManager().saveProject( mastodonProjectPath.toFile() );
		Files.copy( mastodonProjectPath.resolve( "gui.xml" ), mastodonProjectPath.resolve( "gui.xml_remote" ) );
		Files.copy( mastodonProjectPath.resolve( "project.xml" ), mastodonProjectPath.resolve( "project.xml_remote" ) );
		Files.copy( mastodonProjectPath.resolve( "dataset.xml.backup" ), mastodonProjectPath.resolve( "dataset.xml.backup_remote" ) );
		Path gitignore = directory.toPath().resolve( ".gitignore" );
		Files.write( gitignore, "/mastodon.project/gui.xml\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND );
		Files.write( gitignore, "/mastodon.project/project.xml\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND );
		Files.write( gitignore, "/mastodon.project/dataset.xml.backup\n".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND );
		git.add().addFilepattern( ".gitignore" ).call();
		git.commit().setMessage( "Add .gitignore file" ).call();
		git.add().addFilepattern( "mastodon.project" ).call();
		git.commit().setMessage( "Share mastodon project" ).call();
		git.push().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).setRemote( "origin" ).call();
		git.close();
		return new MastodonGitRepository( windowManager );
	}

	private static boolean isEmpty( File directory )
	{
		String[] containedFiles = directory.list();
		return containedFiles == null || containedFiles.length == 0;
	}

	public static void cloneRepository( String repositoryURL, File directory ) throws Exception
	{
		try (Git git = Git.cloneRepository()
				.setURI( repositoryURL )
				.setCredentialsProvider( credentials.getSingleUseCredentialsProvider() )
				.setDirectory( directory )
				.call())
		{
			Path mastodonProjectPath = directory.toPath().resolve( "mastodon.project" );
			Files.copy( mastodonProjectPath.resolve( "gui.xml_remote" ), mastodonProjectPath.resolve( "gui.xml" ) );
			Files.copy( mastodonProjectPath.resolve( "project.xml_remote" ), mastodonProjectPath.resolve( "project.xml" ) );
			Files.copy( mastodonProjectPath.resolve( "dataset.xml.backup_remote" ), mastodonProjectPath.resolve( "dataset.xml.backup" ) );
		}
	}

	public static void openProjectInRepository( Context context, File directory ) throws Exception
	{
		WindowManager windowManager = new WindowManager( context );
		Path path = directory.toPath().resolve( "mastodon.project" );
		windowManager.getProjectManager().openWithDialog( new MamutProjectIO().load( path.toAbsolutePath().toString() ) );
		new MainWindow( windowManager ).setVisible( true );
	}

	public synchronized void commit( String message ) throws Exception
	{
		windowManager.getProjectManager().saveProject();
		commitWithoutSave( message );
	}

	private void commitWithoutSave( String message ) throws Exception
	{
		try (Git git = initGit())
		{
			git.add().addFilepattern( "mastodon.project" ).call();
			CommitCommand commit = git.commit();
			commit.setMessage( message );
			commit.setAuthor( settingsService.getPersonIdent() );
			commit.call();
		}
	}

	public synchronized void push() throws Exception
	{
		try (Git git = initGit())
		{
			Iterable< PushResult > results = git.push().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).setRemote( "origin" ).call();
			raiseExceptionOnUnsuccessfulPush( results );
		}
	}

	private static void raiseExceptionOnUnsuccessfulPush( Iterable< PushResult > results )
	{
		for ( PushResult result : results )
		{
			for ( RemoteRefUpdate update : result.getRemoteUpdates() )
			{
				if ( update.getStatus() == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD )
					throw new RuntimeException( "The remote server has changes, that you didn't download yet.\n"
							+ "Please download changes (pull) first.\n"
							+ "You can upload your changes afterwards.\n" );
				if ( update.getStatus() != RemoteRefUpdate.Status.OK &&
						update.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE )
					throw new RuntimeException( "Push failed: " + update.getMessage() + " " + update.getStatus() );
			}
		}
	}

	public synchronized void createNewBranch( String branchName ) throws Exception
	{
		try (Git git = initGit())
		{
			git.checkout().setCreateBranch( true ).setName( branchName ).call();
		}
	}

	public synchronized void switchBranch( String branchName ) throws Exception
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

	private synchronized String getSimpleName( String branchName )
	{
		String[] parts = branchName.split( "/" );
		return parts[ parts.length - 1 ];
	}

	public synchronized List< String > getBranches() throws Exception
	{
		try (Git git = initGit())
		{
			return git.branchList().setListMode( ListBranchCommand.ListMode.ALL ).call().stream().map( Ref::getName ).collect( Collectors.toList() );
		}
	}

	public synchronized void fetchAll() throws Exception
	{
		try (Git git = initGit())
		{
			git.fetch().setCredentialsProvider( credentials.getSingleUseCredentialsProvider() ).call();
		}
	}

	public synchronized String getCurrentBranch() throws Exception
	{
		try (Git git = initGit())
		{
			return git.getRepository().getFullBranch();
		}
	}

	public synchronized void mergeBranch( String selectedBranch ) throws Exception
	{
		Context context = windowManager.getContext();
		MamutProject project = windowManager.getProjectManager().getProject();
		File projectRoot = project.getProjectRoot();
		try (Git git = initGit())
		{
			windowManager.getProjectManager().saveProject();
			boolean clean = isClean( git );
			if ( !clean )
				throw new RuntimeException( "There are uncommitted changes. Please commit or stash them before merging." );
			String currentBranch = getCurrentBranch();
			Dataset dsA = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setName( selectedBranch ).call();
			Dataset dsB = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setName( currentBranch ).call();
			git.merge().setCommit( false ).include( git.getRepository().exactRef( selectedBranch ) ).call(); // TODO selected branch, should not be a string but a ref instead
			Model mergedModel = merge( dsA, dsB );
			saveModel( context, mergedModel, project );
			commitWithoutSave( "Merge commit generated with Mastodon" );
			reloadFromDisc();
		}
	}

	public synchronized void pull() throws Exception
	{
		Context context = windowManager.getContext();
		MamutProject project = windowManager.getProjectManager().getProject();
		File projectRoot = project.getProjectRoot();
		try (Git git = initGit())
		{
			windowManager.getProjectManager().saveProject();
			boolean isClean = isClean( git );
			if ( !isClean )
				throw new RuntimeException( "There are uncommitted changes. Please commit or stash them before pulling." );
			try
			{
				boolean conflict = !git.pull()
						.setCredentialsProvider( credentials.getSingleUseCredentialsProvider() )
						.setRemote( "origin" )
						.setRebase( false )
						.call().isSuccessful();
				if ( conflict )
					automaticMerge( context, project, projectRoot, git );
			}
			finally
			{
				abortMerge( git );
			}
			reloadFromDisc();
		}
	}

	private void automaticMerge( Context context, MamutProject project, File projectRoot, Git git ) throws Exception
	{
		try
		{
			git.checkout().setAllPaths( true ).setStage( CheckoutCommand.Stage.OURS ).call();
			Dataset dsA = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setAllPaths( true ).setStage( CheckoutCommand.Stage.THEIRS ).call();
			Dataset dsB = new Dataset( projectRoot.getAbsolutePath() );
			git.checkout().setAllPaths( true ).setStage( CheckoutCommand.Stage.OURS ).call();
			Model mergedModel = merge( dsA, dsB );
			if ( ConflictUtils.hasConflict( mergedModel ) )
				throw new GraphMergeConflictException();
			ConflictUtils.removeMergeConflictTagSets( mergedModel );
			saveModel( context, mergedModel, project );
			commitWithoutSave( "Automatic merge by Mastodon during pull" );
		}
		catch ( Throwable t )
		{
			if ( t instanceof GraphMergeException )
				throw t;
			throw new GraphMergeException( "There was a failure, when merging changes to the Model.", t );
		}
	}

	private static void saveModel( Context context, Model model, MamutProject project ) throws IOException
	{
		project.setProjectRoot( project.getProjectRoot() );
		try (final MamutProject.ProjectWriter writer = project.openForWriting())
		{
			new MamutProjectIO().save( project, writer );
			final RawGraphIO.GraphToFileIdMap< Spot, Link > idmap = model.saveRaw( writer );
			MamutRawFeatureModelIO.serialize( context, model, idmap, writer );
		}
	}

	private static Model merge( Dataset dsA, Dataset dsB ) throws IOException, SpimDataException
	{
		final MergeDatasets.OutputDataSet output = new MergeDatasets.OutputDataSet();
		double distCutoff = 1000;
		double mahalanobisDistCutoff = 1;
		double ratioThreshold = 2;
		MergeDatasets.merge( dsA, dsB, output, distCutoff, mahalanobisDistCutoff, ratioThreshold );
		return output.getModel();
	}

	private synchronized void reloadFromDisc() throws IOException, SpimDataException
	{
		MamutProject project = windowManager.getProjectManager().getProject();
		windowManager.getProjectManager().openWithDialog( project );
	}

	public synchronized void reset() throws Exception
	{
		try (Git git = initGit())
		{
			git.reset().setMode( ResetCommand.ResetType.HARD ).call();
			reloadFromDisc();
		}
	}

	private synchronized Git initGit() throws IOException
	{
		File projectRoot = windowManager.getProjectManager().getProject().getProjectRoot();
		return initGit( projectRoot );
	}

	private synchronized Git initGit( File projectRoot ) throws IOException
	{
		boolean correctFolder = projectRoot.getName().equals( "mastodon.project" );
		if ( !correctFolder )
			throw new RuntimeException( "The current project does not appear to be in a git repo." );
		File gitRoot = projectRoot.getParentFile();
		if ( !new File( gitRoot, ".git" ).exists() )
			throw new RuntimeException( "The current project does not appear to be in a git repo." );
		return Git.open( gitRoot );
	}

	private synchronized boolean isClean( Git git ) throws GitAPIException
	{
		return git.status().call().isClean();
	}

	public boolean isRepository()
	{
		try (Git ignored = initGit())
		{
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	public static void main( String... args )
	{

		File dir = new File( "/home/arzt/tmp/2/mgit-test" );
		File projectRoot = new File( dir, "mastodon.project" );
		try (Git git = Git.open( dir ); Context context = new Context())
		{
			git.checkout()
					.addPath( "mastodon.project/gui.xml" )
					.addPath( "mastodon.project/model.raw" )
					.addPath( "mastodon.project/tags.raw" )
					.addPath( "mastodon.project/ksdfghksdgh.raw" )
					.call();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private static void abortMerge( Git git ) throws Exception
	{
		Repository repository = git.getRepository();
		repository.writeMergeCommitMsg( null );
		repository.writeMergeHeads( null );
		git.reset().setMode( ResetCommand.ResetType.HARD ).call();
	}

	public void resetToRemoteBranch() throws Exception
	{
		try (Git git = initGit())
		{
			Repository repository = git.getRepository();
			String remoteTrackingBranch = new BranchConfig( repository.getConfig(), repository.getBranch() ).getRemoteTrackingBranch();
			git.reset().setMode( ResetCommand.ResetType.HARD ).setRef( remoteTrackingBranch ).call();
			reloadFromDisc();
		}
	}
}
