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

@Plugin( type = ImageJService.class )
public class LineageRegistrationControlService extends AbstractService implements ImageJService
{
	private final LineageRegistrationDialog dialog = new LineageRegistrationDialog( new Listener() );

	private final List< WindowManager > windowManagers = new ArrayList<>();

	public void registerMastodonInstance( WindowManager windowManager )
	{
		windowManagers.add( windowManager );
	}

	public void showDialog()
	{
		if ( dialog.isVisible() )
			return;
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

	private class Listener implements LineageRegistrationDialog.Listener
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
			sortSecondTrackScheme( getModelB(), getModelA() );
		}

		@Override
		public void onSortTrackSchemeBClicked()
		{
			sortSecondTrackScheme( getModelA(), getModelB() );
		}

		private void sortSecondTrackScheme( Model modelA, Model modelB )
		{
			executeTask( true, modelA, modelB, () -> {
				LineageRegistrationUtils.sortSecondTrackSchemeToMatch( modelA, modelB );
				modelB.setUndoPoint();
			} );
		}

		@Override
		public void onColorLineagesClicked()
		{
			Model modelA = getModelA();
			Model modelB = getModelB();
			executeTask( false, modelA, modelB, () -> {
				LineageColoring.tagLineages( modelA, modelB );
				modelA.setUndoPoint();
				modelB.setUndoPoint();
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

			List< TagSetStructure.TagSet > tagSets = fromModel.getTagSetModel().getTagSetStructure().getTagSets();
			if ( tagSets.isEmpty() )
			{
				JOptionPane.showMessageDialog( dialog,
						"No tag sets in project \"" + LineageRegistrationDialog.getProjectName( fromProject ) + "\"." );
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
				String newTagSetName = tagSet.getName() + " (" + LineageRegistrationDialog.getProjectName( fromProject ) + ")";
				LineageRegistrationUtils.copyTagSetToSecond( fromModel, toModel, tagSet, newTagSetName );
				toModel.setUndoPoint();
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
			Model modelA = getModelA();
			Model modelB = getModelB();
			executeTask( false, modelA, modelB, () -> {
				LineageRegistrationUtils.tagCells( modelA, modelB, modifyA, modifyB );
				if ( modifyA )
					modelA.setUndoPoint();
				if ( modifyB )
					modelB.setUndoPoint();
			} );
		}

		@Override
		public void onSyncGroupClicked( int i )
		{
			if ( coupling != null )
				coupling.close();
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
			coupling = new ModelCoupling( appModelA, appModelB, r, i );
		}

		private Model getModelB()
		{
			return dialog.getProjectB().getAppModel().getModel();
		}

		private Model getModelA()
		{
			return dialog.getProjectA().getAppModel().getModel();
		}
	}
}
