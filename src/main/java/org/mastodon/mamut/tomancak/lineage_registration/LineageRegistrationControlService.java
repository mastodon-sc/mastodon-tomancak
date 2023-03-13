package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import net.imagej.ImageJService;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.tomancak.lineage_registration.coupling.ModelCoupling;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 * This class is the controller for the {@link LineageRegistrationFrame}.
 * It shows the dialog and performs the actions requested by the user.
 * <p>
 * There should be only one instance of this class in the Fiji application.
 * This is ensured by making it an {@link ImageJService}. Being a service,
 * allows the {@link LineageRegistrationPlugin} to access it, and to call
 * {@link #registerMastodonInstance(WindowManager)}.
 *
 * @author Matthias Arzt
 */
@Plugin( type = ImageJService.class )
public class LineageRegistrationControlService extends AbstractService implements ImageJService
{
	private final LineageRegistrationFrame dialog = new LineageRegistrationFrame( new Listener() );

	private final List< WindowManager > windowManagers = new ArrayList<>();

	public void registerMastodonInstance( WindowManager windowManager )
	{
		windowManagers.add( windowManager );
	}

	public void showDialog()
	{
		if ( dialog.isVisible() )
		{
			dialog.toFront();
			return;
		}
		dialog.setMastodonInstances( windowManagers );
		dialog.pack();
		dialog.setVisible( true );
	}

	/** Executes the specified task in a new thread, while locking both models. */
	private static void executeTask( boolean writeLock, Model modelA, Model modelB, Runnable task )
	{
		new Thread( () -> {
			ReentrantReadWriteLock readWriteLockA = modelA.getGraph().getLock();
			ReentrantReadWriteLock readWriteLockB = modelB.getGraph().getLock();
			Lock lockA = writeLock ? readWriteLockA.writeLock() : readWriteLockA.readLock();
			Lock lockB = writeLock ? readWriteLockB.writeLock() : readWriteLockB.readLock();
			try ( ClosableLock ignored = LockUtils.lockBoth( lockA, lockB ) )
			{
				task.run();
			}
		} ).start();
	}

	private class Listener implements LineageRegistrationFrame.Listener
	{

		private ModelCoupling coupling = null;

		@Override
		public void onUpdateClicked()
		{
			dialog.setMastodonInstances( windowManagers );
		}

		@Override
		public void onSortTrackSchemeAClicked()
		{
			sortSecondTrackScheme( dialog.getProjectB(), dialog.getProjectA() );
		}

		@Override
		public void onSortTrackSchemeBClicked()
		{
			sortSecondTrackScheme( dialog.getProjectA(), dialog.getProjectB() );
		}

		private void sortSecondTrackScheme( WindowManager project1, WindowManager project2 )
		{
			String projectTitle1 = LineageRegistrationFrame.getProjectName( project1 );
			String projectTitle2 = LineageRegistrationFrame.getProjectName( project2 );
			Model model1 = project1.getAppModel().getModel();
			Model model2 = project2.getAppModel().getModel();
			executeTask( true, model1, model2, () -> {
				dialog.clearLog();
				dialog.log( "Sort the order of the child cells in the TrackScheme of project \"%s\".", projectTitle2 );
				dialog.log( "Use project \"%s\" as reference...", projectTitle1 );
				LineageRegistrationUtils.sortSecondTrackSchemeToMatch( model1, model2 );
				project2.getAppModel().getBranchGraphSync().sync();
				model2.setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onColorLineagesClicked()
		{
			WindowManager projectA = dialog.getProjectA();
			WindowManager projectB = dialog.getProjectB();
			String titleA = LineageRegistrationFrame.getProjectName( projectA );
			String titleB = LineageRegistrationFrame.getProjectName( projectB );
			Model modelA = projectA.getAppModel().getModel();
			Model modelB = projectB.getAppModel().getModel();
			executeTask( false, modelA, modelB, () -> {
				dialog.clearLog();
				dialog.log( "Create tag set \"lineages\" in project \"%s\"...", titleA );
				dialog.log( "Create tag set \"lineages\" in project \"%s\"...", titleB );
				LineageColoring.tagLineages( modelA, modelB );
				modelA.setUndoPoint();
				modelB.setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onCopyTagSetAtoB()
		{
			copyTagSetFromTo( dialog.getProjectA(), dialog.getProjectB() );
		}

		@Override
		public void onCopyTagSetBtoA()
		{
			copyTagSetFromTo( dialog.getProjectB(), dialog.getProjectA() );
		}

		private void copyTagSetFromTo( WindowManager fromProject, WindowManager toProject )
		{
			Model fromModel = fromProject.getAppModel().getModel();
			Model toModel = toProject.getAppModel().getModel();
			String fromProjectTitle = LineageRegistrationFrame.getProjectName( fromProject );
			String toProjectTitle = LineageRegistrationFrame.getProjectName( toProject );

			List< TagSetStructure.TagSet > tagSets = fromModel.getTagSetModel().getTagSetStructure().getTagSets();
			if ( tagSets.isEmpty() )
			{
				JOptionPane.showMessageDialog( dialog,
						"No tag sets in project \"" + fromProjectTitle + "\"." );
				return;
			}

			TagSetStructure.TagSet tagSet = ComboBoxDialog.showComboBoxDialog( dialog,
					"Copy tag set to registered lineage",
					"Select tag set to copy:",
					tagSets,
					TagSetStructure.TagSet::getName );

			if ( tagSet == null )
				return;

			executeTask( false, fromModel, toModel, () -> {
				dialog.clearLog();
				dialog.log( "Copy tag set \"%s\" from project \"%s\" to project \"%s\"...",
						tagSet.getName(), fromProjectTitle, toProjectTitle );
				String newTagSetName = tagSet.getName() + " (" + fromProjectTitle + ")";
				LineageRegistrationUtils.copyTagSetToSecond( fromModel, toModel, tagSet, newTagSetName );
				toModel.setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onTagBothClicked()
		{
			putTags( true, true );
		}

		@Override
		public void onTagProjectAClicked()
		{
			putTags( true, false );
		}

		@Override
		public void onTagProjectBClicked()
		{
			putTags( false, true );
		}

		private void putTags( boolean modifyA, boolean modifyB )
		{
			WindowManager projectA = dialog.getProjectA();
			WindowManager projectB = dialog.getProjectB();
			String titleA = LineageRegistrationFrame.getProjectName( projectA );
			String titleB = LineageRegistrationFrame.getProjectName( projectB );
			Model modelA = projectA.getAppModel().getModel();
			Model modelB = projectB.getAppModel().getModel();
			executeTask( false, modelA, modelB, () -> {
				dialog.clearLog();
				if ( modifyA )
					dialog.log( "Create tag set \"lineage registration\" in project \"%s\"...", titleA );
				if ( modifyB )
					dialog.log( "Create tag set \"lineage registration\" in project \"%s\"...", titleB );
				LineageRegistrationUtils.tagCells( modelA, modelB, modifyA, modifyB );
				if ( modifyA )
					modelA.setUndoPoint();
				if ( modifyB )
					modelB.setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onSyncGroupClicked( int i )
		{
			dialog.clearLog();
			if ( coupling != null )
			{
				dialog.log( "... stop synchronization between projects." );
				coupling.close();
			}
			coupling = null;
			if ( i < 0 )
				return;
			MamutAppModel appModelA = dialog.getProjectA().getAppModel();
			MamutAppModel appModelB = dialog.getProjectB().getAppModel();
			ModelGraph graphA = appModelA.getModel().getGraph();
			ModelGraph graphB = appModelB.getModel().getGraph();
			RegisteredGraphs r;
			try ( ClosableLock ignored = LockUtils.lockBoth(
					graphA.getLock().readLock(),
					graphB.getLock().readLock() ) )
			{
				r = LineageRegistrationAlgorithm.run(
						graphA,
						graphB );
			}
			dialog.log( "Synchronize focused and highlighted spot between project A and project B." );
			dialog.log( "Synchronize navigate to spot actions between project A and project B. (sync. group %d)", i + 1 );
			coupling = new ModelCoupling( appModelA, appModelB, r, i );
		}

	}
}
