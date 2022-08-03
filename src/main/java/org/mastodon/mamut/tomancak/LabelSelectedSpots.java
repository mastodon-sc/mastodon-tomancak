/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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
