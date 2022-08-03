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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public class CopyTagDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private final TagSelectionPanel tspFrom;

	private final TagSelectionPanel tspTo;

	private final Model model;

	public CopyTagDialog( final Frame owner, final Model model )
	{
		super( owner, "Copy Tag...", false );
		this.model = model;

		model.getTagSetModel().listeners().add( this::tagSetStructureChanged );
		tspFrom = new TagSelectionPanel();
		tspTo = new TagSelectionPanel();
		tagSetStructureChanged();

		final JPanel content = new JPanel();
		content.setLayout( new GridBagLayout() );
		content.setBorder( BorderFactory.createEmptyBorder( 30, 20, 20, 20 ) );

		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		content.add( new JLabel( "If has tag " ), c );
		++c.gridx;
		content.add( tspFrom, c );
		++c.gridx;
		content.add( new JLabel( " then also assign " ), c );
		++c.gridx;
		content.add( tspTo, c );

		final JPanel buttons = new JPanel();
		final JButton closeButton = new JButton( "Done" );
		closeButton.addActionListener( e -> close() );
		final JButton copyTagButton = new JButton( "Copy Tag" );
		copyTagButton.addActionListener( e -> copyTag() );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( closeButton );
		buttons.add( copyTagButton );

		getContentPane().add( content, BorderLayout.CENTER );
		getContentPane().add( buttons, BorderLayout.SOUTH );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				close();
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}

	private void tagSetStructureChanged()
	{
		final TagSetStructure tss = model.getTagSetModel().getTagSetStructure();
		tspFrom.refreshTagSets( tss );
		tspTo.refreshTagSets( tss );
		pack();
	}

	private void close()
	{
		setVisible( false );
	}

	private void copyTag()
	{
		final Tag from = tspFrom.getSelected();
		final Tag to = tspTo.getSelected();
		final ReentrantReadWriteLock lock = model.getGraph().getLock();
		lock.readLock().lock();
		try
		{
			final TagSetModel< Spot, Link > tsm = model.getTagSetModel();
			final ObjTags< Spot > vertexTags = tsm.getVertexTags();
			for ( final Spot spot : vertexTags.getTaggedWith( from ) )
				vertexTags.set( spot, to );
			final ObjTags< Link > edgeTags = tsm.getEdgeTags();
			for ( final Link link : edgeTags.getTaggedWith( from ) )
				edgeTags.set( link, to );
			model.setUndoPoint();
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	static class Item< T >
	{
		private final T item;
		private final String label;

		public Item( final T item, final String label )
		{
			this.item = item;
			this.label = label;
		}

		public T get()
		{
			return item;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	static class TagSelectionPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JComboBox< Item< TagSet > > cbTagSets;
		private final JComboBox< Item< Tag > > cbTags;

		TagSelectionPanel( )
		{
			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );

			this.cbTagSets = new JComboBox<>();
			cbTagSets.addItemListener( e -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
					refreshTags();
			} );
			add( cbTagSets );


			add( new JLabel( "-" ) );

			cbTags = new JComboBox<>();
			add( cbTags );

			refreshTagSets( null );
		}

		private void refreshTagSets( final TagSetStructure tss )
		{
			if ( tss == null )
			{
				cbTagSets.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				@SuppressWarnings( "unchecked" )
				final Item< TagSet >[] tagSetItems = tss.getTagSets()
						.stream()
						.map( tagset -> new Item<>( tagset, tagset.getName() ) )
						.toArray( Item[]::new );
				cbTagSets.setModel( new DefaultComboBoxModel<>( tagSetItems ) );
				cbTagSets.setSelectedIndex( 0 );
			}
			refreshTags();
		}

		private void refreshTags()
		{
			@SuppressWarnings( "unchecked" )
			final Item< TagSet > tagSetItem = ( Item< TagSet > ) cbTagSets.getSelectedItem();
			if ( tagSetItem == null )
			{
				cbTags.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				final TagSet ts = tagSetItem.get();
				@SuppressWarnings( "unchecked" )
				final Item< Tag >[] tagItems = ts.getTags()
						.stream()
						.map( tag -> new Item<>( tag, tag.label() ) )
						.toArray( Item[]::new );
				cbTags.setModel( new DefaultComboBoxModel<>( tagItems ) );
			}
		}

		void setTagSetStructure( final TagSetStructure tss )
		{
			refreshTagSets( tss );
		}

		Tag getSelected()
		{
			@SuppressWarnings( "unchecked" )
			final Item< Tag > tagItem = ( Item< Tag > ) cbTags.getSelectedItem();
			return tagItem == null ? null : tagItem.get();
		}
	}
}
