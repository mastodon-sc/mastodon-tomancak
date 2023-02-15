package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ImageJService;

import org.mastodon.app.ui.ViewFrame;
import org.mastodon.mamut.MamutViewBdv;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
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

	private void sortTrackSchemeReferenceAndModified( WindowManager reference, WindowManager modified )
	{
		Model referenceModel = reference.getAppModel().getModel();
		Model modifiedModel = modified.getAppModel().getModel();
		LineageRegistrationAlgorithm.run( referenceModel, modifiedModel );
	}

	private class Listener implements LineageRegistrationDialog.Listener
	{

		@Override
		public void onUpdateClicked()
		{
			dialog.setMastodonInstances( windowManagers );
		}

		@Override
		public void onImproveTitlesClicked()
		{
			for ( WindowManager windowManager : windowManagers )
				improveTitles( windowManager );
		}

		private void improveTitles( WindowManager windowManager )
		{
			// NB: kind of a hack and not as good as it should be.
			String projectName = LineageRegistrationDialog.getProjectName( windowManager );
			for ( MamutViewBdv window : windowManager.getBdvWindows() )
			{
				ViewFrame frame = window.getFrame();
				String title = frame.getTitle();
				frame.setTitle( title.split( " - ", 2 )[ 0 ] + " - " + projectName );
			}
		}

		@Override
		public void onSortTrackSchemeAClicked()
		{
			sortTrackSchemeReferenceAndModified( dialog.getProjectB(), dialog.getProjectA() );
		}

		@Override
		public void onSortTrackSchemeBClicked()
		{
			sortTrackSchemeReferenceAndModified( dialog.getProjectA(), dialog.getProjectB() );
		}

		@Override
		public void onColorLineagesClicked()
		{
			Model modelA = dialog.getProjectA().getAppModel().getModel();
			Model modelB = dialog.getProjectB().getAppModel().getModel();
			LineageColoring.tagLineages( modelA, modelB );
		}

		@Override
		public void onCopyTagSetAtoB()
		{
			// TODO
		}

		@Override
		public void onCopyTagSetBtoA()
		{
			// TODO
		}
	}
}
