package org.mastodon.mamut.tomancak;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import org.mastodon.mamut.MamutAppModel;
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
	public static void run( MamutAppModel appModel )
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
		String message = "Mirror the X coordinate of each spot.\n"
				+ "\n"
				+ "The plugin first calculates the mean X coordinate of all spots."
				+ "Then the X-coordinate of each spot is mirrored around the"
				+ "plane x = mean X. The ellipsoids are mirrored as well.\n"
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
