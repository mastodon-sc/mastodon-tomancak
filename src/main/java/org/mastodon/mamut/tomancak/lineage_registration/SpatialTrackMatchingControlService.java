/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import net.imagej.ImageJService;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.angle_feature.CellDivisionAngleFeature;
import org.mastodon.mamut.tomancak.lineage_registration.coupling.ModelCoupling;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 * This class is the controller for the {@link SpatialTrackMatchingFrame}.
 * It shows the dialog and performs the actions requested by the user.
 * <p>
 * There should be only one instance of this class in the Fiji application.
 * This is ensured by making it an {@link ImageJService}. Being a service,
 * allows the {@link SpatialTrackMatchingPlugin} to access it, and to call
 * {@link #registerMastodonInstance}.
 *
 * @author Matthias Arzt
 */
@Plugin( type = ImageJService.class )
public class SpatialTrackMatchingControlService extends AbstractService implements ImageJService
{
	private SpatialTrackMatchingFrame dialog = null;

	private final List< ProjectModel > projectModels = new ArrayList<>();

	public void registerMastodonInstance( ProjectModel projectModel )
	{
		projectModels.add( projectModel );
		if ( dialog != null )
			dialog.setMastodonInstances( projectModels );
	}

	public void unregisterMastodonInstance( ProjectModel projectModel )
	{
		projectModels.remove( projectModel );
		if ( dialog != null )
			dialog.setMastodonInstances( projectModels );
	}

	public synchronized void showDialog()
	{
		if ( dialog != null && dialog.isVisible() )
		{
			dialog.toFront();
			return;
		}
		if ( dialog == null )
			dialog = new SpatialTrackMatchingFrame( new Listener() );
		dialog.setMastodonInstances( projectModels );
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

	private class Listener implements SpatialTrackMatchingFrame.Listener
	{

		private ModelCoupling coupling = null;

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
			executeTask( true, project1, project2, () -> {
				dialog.clearLog();
				dialog.log( "Sort the order of the child cells in the TrackScheme of project \"%s\".", project1.getName() );
				dialog.log( "Use project \"%s\" as reference...", project2.getName() );
				RegisteredGraphs registration = runRegistrationAlgorithm( project1, project2 );
				SpatialTrackMatchingUtils.sortSecondTrackSchemeToMatch( registration );
				project2.getProjectModel().getBranchGraphSync().sync();
				project2.getModel().setUndoPoint();
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
				TrackColoring.tagLineages(
						projectA.getModel(), projectA.getFirstTimepoint(),
						projectB.getModel(), projectB.getFirstTimepoint() );
				projectA.getModel().setUndoPoint();
				projectB.getModel().setUndoPoint();
				dialog.log( "done." );
			} );
		}

		@Override
		public void onCopyLabelsAtoB()
		{
			copyLabelsFromTo( dialog.getProjectA(), dialog.getProjectB() );
		}

		@Override
		public void onCopyLabelsBtoA()
		{
			copyLabelsFromTo( dialog.getProjectB(), dialog.getProjectA() );
		}

		private void copyLabelsFromTo( SelectedProject fromProject, SelectedProject toProject )
		{
			executeTask( true, fromProject, toProject, () -> {
				dialog.clearLog();
				dialog.log( "Copy labels from project \"%s\" to project \"%s\"...",
						fromProject.getName(), toProject.getName() );
				RegisteredGraphs registration = runRegistrationAlgorithm( fromProject, toProject );
				SpatialTrackMatchingUtils.copySpotLabelsFromAtoB( registration );
				toProject.getModel().setUndoPoint();
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
				RegisteredGraphs registration = runRegistrationAlgorithm( fromProject, toProject );
				SpatialTrackMatchingUtils.copyTagSetToSecondModel( registration, tagSet, newTagSetName );
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
					dialog.log( "Create tag set \"spatial track matching\" in project \"%s\"...", projectA.getName() );
				if ( modifyB )
					dialog.log( "Create tag set \"spatial track matching\" in project \"%s\"...", projectB.getName() );
				RegisteredGraphs registration = runRegistrationAlgorithm( projectA, projectB );
				SpatialTrackMatchingUtils.tagCells( registration, modifyA, modifyB );
				if ( modifyA )
					projectA.getModel().setUndoPoint();
				if ( modifyB )
					projectB.getModel().setUndoPoint();
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
				r = runRegistrationAlgorithm( projectA, projectB );
			}
			dialog.log( "Synchronize focused and highlighted spot between project A and project B." );
			dialog.log( "Synchronize navigate to spot actions between project A and project B. (sync. group %d)", i + 1 );
			coupling = new ModelCoupling( projectA.getProjectModel(), projectB.getProjectModel(), r, i );
		}

		@Override
		public void onPlotAnglesClicked()
		{
			SelectedProject projectA = dialog.getProjectA();
			SelectedProject projectB = dialog.getProjectB();
			RegisteredGraphs registeredGraphs = runRegistrationAlgorithm( projectA, projectB );
			RefDoubleMap< Spot > anglesA = registeredGraphs.anglesA;
			SpatialTrackMatchingUtils.plotAngleAgainstTimepoint( anglesA );
		}

		@Override
		public void onAddAnglesFeatureClicked()
		{
			SelectedProject projectA = dialog.getProjectA();
			SelectedProject projectB = dialog.getProjectB();
			RegisteredGraphs registeredGraphs = runRegistrationAlgorithm( projectA, projectB );
			CellDivisionAngleFeature.declare( registeredGraphs.modelA, registeredGraphs.anglesA );
			CellDivisionAngleFeature.declare( registeredGraphs.modelB, registeredGraphs.anglesB );
		}

	}

	private RegisteredGraphs runRegistrationAlgorithm( SelectedProject projectA, SelectedProject projectB )
	{
		return SpatialTrackMatchingAlgorithm.run(
				projectA.getModel(), projectA.getFirstTimepoint(),
				projectB.getModel(), projectB.getFirstTimepoint(),
				dialog.getSpatialRegistrationMethod() );
	}
}
