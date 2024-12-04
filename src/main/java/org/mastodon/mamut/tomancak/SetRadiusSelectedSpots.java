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
