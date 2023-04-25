package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import net.imagej.ImageJService;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
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
	private static void executeTask( boolean writeLock, SelectedProject projectA, SelectedProject projectB, Runnable task )
	{
		new Thread( () -> {
			ReentrantReadWriteLock readWriteLockA = projectA.getGraph().getLock();
			ReentrantReadWriteLock readWriteLockB = projectB.getGraph().getLock();
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

		private void sortSecondTrackScheme( SelectedProject project1, SelectedProject project2 )
		{
			Model model1 = project1.getAppModel().getModel();
			Model model2 = project2.getAppModel().getModel();
			executeTask( true, project1, project2, () -> {
				dialog.clearLog();
				dialog.log( "Sort the order of the child cells in the TrackScheme of project \"%s\".", project1.getName() );
				dialog.log( "Use project \"%s\" as reference...", project2.getName() );
				LineageRegistrationUtils.sortSecondTrackSchemeToMatch( model1, model2 );
				project2.getAppModel().getBranchGraphSync().sync();
				model2.setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onColorLineagesClicked()
		{
			SelectedProject projectA = dialog.getProjectA();
			SelectedProject projectB = dialog.getProjectB();
			executeTask( false, projectA, projectB, () -> {
				dialog.clearLog();
				dialog.log( "Create tag set \"lineages\" in project \"%s\"...", projectA.getName() );
				dialog.log( "Create tag set \"lineages\" in project \"%s\"...", projectB.getName() );
				LineageColoring.tagLineages( projectA.getModel(), projectB.getModel() );
				projectA.getModel().setUndoPoint();
				projectB.getModel().setUndoPoint();
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

		private void copyTagSetFromTo( SelectedProject fromProject, SelectedProject toProject )
		{
			Model fromModel = fromProject.getModel();
			Model toModel = toProject.getModel();

			List< TagSetStructure.TagSet > tagSets = fromModel.getTagSetModel().getTagSetStructure().getTagSets();
			if ( tagSets.isEmpty() )
			{
				JOptionPane.showMessageDialog( dialog,
						"No tag sets in project \"" + fromProject.getName() + "\"." );
				return;
			}

			TagSetStructure.TagSet tagSet = ComboBoxDialog.showComboBoxDialog( dialog,
					"Copy tag set to registered lineage",
					"Select tag set to copy:",
					tagSets,
					TagSetStructure.TagSet::getName );

			if ( tagSet == null )
				return;

			executeTask( false, fromProject, toProject, () -> {
				dialog.clearLog();
				dialog.log( "Copy tag set \"%s\" from project \"%s\" to project \"%s\"...",
						tagSet.getName(), fromProject.getName(), toProject.getName() );
				String newTagSetName = tagSet.getName() + " (" + fromProject.getName() + ")";
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
			SelectedProject projectA = dialog.getProjectA();
			SelectedProject projectB = dialog.getProjectB();
			executeTask( false, projectA, projectB, () -> {
				dialog.clearLog();
				if ( modifyA )
					dialog.log( "Create tag set \"lineage registration\" in project \"%s\"...", projectA.getName() );
				if ( modifyB )
					dialog.log( "Create tag set \"lineage registration\" in project \"%s\"...", projectB.getName() );
				Model modelA = projectA.getModel();
				Model modelB = projectB.getModel();
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
			SelectedProject projectA = dialog.getProjectA();
			SelectedProject projectB = dialog.getProjectB();
			RegisteredGraphs r;
			try ( ClosableLock ignored = LockUtils.lockBoth(
					projectA.getGraph().getLock().readLock(),
					projectB.getGraph().getLock().readLock() ) )
			{
				r = LineageRegistrationAlgorithm.run(
						projectA.getGraph(),
						projectB.getGraph() );
			}
			dialog.log( "Synchronize focused and highlighted spot between project A and project B." );
			dialog.log( "Synchronize navigate to spot actions between project A and project B. (sync. group %d)", i + 1 );
			coupling = new ModelCoupling( projectA.getAppModel(), projectB.getAppModel(), r, i );
		}
	}
}
