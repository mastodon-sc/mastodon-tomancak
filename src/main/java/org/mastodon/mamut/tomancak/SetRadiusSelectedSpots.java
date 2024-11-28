package org.mastodon.mamut.tomancak;

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

public class SetRadiusSelectedSpots
{
	private SetRadiusSelectedSpots()
	{
		// Private constructor to prevent instantiation.
	}

	static void setRadiusSelectedSpot( final ProjectModel projectModel, final CommandService commandService )
	{
		final SelectionModel< Spot, Link > selection = projectModel.getSelectionModel();
		final Set< Spot > spots = selection.getSelectedVertices();
		if ( spots.isEmpty() )
			JOptionPane.showMessageDialog( null, "No spot selected.", "Set radius of selected spots", JOptionPane.WARNING_MESSAGE );
		else
			commandService.run( SetRadiusCommand.class, true, "projectModel", projectModel );
	}

	@Plugin( type = Command.class, label = "Set radius of selected spots" )
	public static class SetRadiusCommand implements Command
	{

		@Parameter
		private ProjectModel projectModel;

		@Parameter
		private Context context;

		@Parameter( label = "Spot radius", description = "Specify the radius you want to set for the selected spots.", min = "0", validater = "validateRadius", initializer = "initRadius" )
		private double radius;

		@Override
		public void run()
		{
			if ( radius <= 0 )
				return;
			final SelectionModel< Spot, Link > selection = projectModel.getSelectionModel();
			final Model model = projectModel.getModel();
			final ReentrantReadWriteLock lock = model.getGraph().getLock();
			lock.writeLock().lock();

			try
			{
				final Set< Spot > spots = selection.getSelectedVertices();
				double[][] covariance = covarianceFromRadiusSquared( radius * radius );
				spots.forEach( spot -> spot.setCovariance( covariance ) );
				model.setUndoPoint();
			}
			finally
			{
				lock.writeLock().unlock();
			}
			model.getGraph().notifyGraphChanged();
		}

		@SuppressWarnings( "unused" )
		private void validateRadius()
		{
			if ( radius <= 0 )
				JOptionPane.showMessageDialog( null, "Selected radius must > 0.", "Non positive radius.",
						JOptionPane.ERROR_MESSAGE );
		}

		@SuppressWarnings( "unused" )
		private void initRadius()
		{
			if ( radius <= 0 )
				radius = Math
						.sqrt( projectModel.getSelectionModel().getSelectedVertices().iterator().next().getBoundingSphereRadiusSquared() );
		}

		private static double[][] covarianceFromRadiusSquared( final double radiusSquared )
		{
			final double[][] covariance = new double[ 3 ][ 3 ];
			for ( int row = 0; row < 3; ++row )
				for ( int col = 0; col < 3; ++col )
					covariance[ row ][ col ] = ( row == col ) ? radiusSquared : 0;
			return covariance;
		}
	}
}
