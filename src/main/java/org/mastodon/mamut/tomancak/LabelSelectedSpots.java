package org.mastodon.mamut.tomancak;

import javax.swing.JOptionPane;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LabelSelectedSpots
{

	static void labelSelectedSpot( MamutAppModel appModel )
	{
		final SelectionModel<Spot, Link> selection = appModel.getSelectionModel();
		final Model model = appModel.getModel();
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.writeLock().lock();
		try
		{
			final Set< Spot > spots = selection.getSelectedVertices();
			if ( spots.isEmpty() )
			{
				JOptionPane.showMessageDialog( null, "No spot selected.", "Label spots", JOptionPane.WARNING_MESSAGE );
			}
			else
			{
				final String initialValue = spots.iterator().next().getLabel();
				final Object input = JOptionPane.showInputDialog( null, "Spot label:", "Label spots", JOptionPane.PLAIN_MESSAGE, null, null, initialValue );
				if ( input != null )
				{
					final String label = ( String ) input;
					spots.forEach( spot -> spot.setLabel( label ) );
					model.setUndoPoint();
				}
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
}
