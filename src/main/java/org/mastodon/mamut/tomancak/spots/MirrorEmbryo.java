/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.spots;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Plugin that mirrors the X coordinate of each spot.
 */
public class MirrorEmbryo
{
	private MirrorEmbryo()
	{
		// prevent from instantiation
	}

	/**
	 * Shows a warning dialog and mirrors all spots along the X axis.
	 */
	public static void run( ProjectModel appModel )
	{
		if ( !showDialog() )
			return;
		Model model = appModel.getModel();
		ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			mirrorX( graph );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
	}

	private static boolean showDialog()
	{
		String title = "Mirror X coordinate of each spot";
		String message = "Mirror the x-coordinate of each spot.\n"
				+ "\n"
				+ "The plugin first calculates the mean x-coordinate of all spots."
				+ "Then the x-coordinate of each spot is mirrored on the"
				+ "plane x = mean x. The ellipsoids are mirrored as well.\n"
				+ "\n"
				+ "Please note: The plugin does not support mirroring of the image data.\n"
				+ "The spots will therefore appear to be in the wrong place.\n"
				+ "\n"
				+ "Do you want to continue?";
		String[] buttons = { "Mirror", "Cancel" };
		String defaultButton = "Mirror";
		int result = JOptionPane.showOptionDialog( null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, buttons, defaultButton );
		return result == JOptionPane.YES_OPTION;
	}

	public static void mirrorX( ModelGraph graph )
	{
		double meanX = computeMeanX( graph );
		double[][] covariance = new double[ 3 ][ 3 ];
		for ( Spot spot : graph.vertices() )
		{
			mirrorSpotPosition( meanX, spot );
			mirrorCovarianceMatrix( covariance, spot );
		}
	}

	private static double computeMeanX( ModelGraph graph )
	{
		double sumX = 0;
		long count = 0;
		for ( Spot spot : graph.vertices() )
		{
			sumX += spot.getDoublePosition( 0 );
			count++;
		}
		return sumX / count;
	}

	private static void mirrorSpotPosition( double meanX, Spot spot )
	{
		spot.setPosition( 2 * meanX - spot.getDoublePosition( 0 ), 0 );
	}

	private static void mirrorCovarianceMatrix( double[][] covariance, Spot spot )
	{
		spot.getCovariance( covariance );
		covariance[ 0 ][ 1 ] = -covariance[ 0 ][ 1 ];
		covariance[ 0 ][ 2 ] = -covariance[ 0 ][ 2 ];
		covariance[ 1 ][ 0 ] = -covariance[ 1 ][ 0 ];
		covariance[ 2 ][ 0 ] = -covariance[ 2 ][ 0 ];
		spot.setCovariance( covariance );
	}
}
