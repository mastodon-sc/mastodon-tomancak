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
package org.mastodon.mamut.tomancak.sort_tree;

import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * This component allows the user to select a tag or lineage in the {@link Model}.
 * <p>
 * {@link #getSelectedSpots()} will return either all the spots that have the
 * selected tag, or all the spots that belong to the selected lineage.
 * <p>
 * The methods {@link #addEntireGraphItem()} and {@link #addSelectedNodesItem()}
 * will add two more options from which the uses may chose: 1. entire graph
 * 2. selected nodes.
 *
 * @author Matthias Arzt
 */
public class SelectSpotsComponent extends JButton
{
	private static final String ENTIRE_GRAPH = "entire graph";

	private static final String SELECTED_NODES = "selected nodes";

	private final MamutAppModel appModel;

	private final Model mastodonModel;

	private Object selected;

	private boolean hasEntireGraphItem = false;

	private boolean hasSelectedNodesItem = false;

	public SelectSpotsComponent( MamutAppModel appModel )
	{
		super( "<please select>" );
		this.appModel = appModel;
		this.mastodonModel = appModel.getModel();
		this.addActionListener( ignore -> clicked() );
	}

	public void addEntireGraphItem()
	{
		this.hasEntireGraphItem = true;
	}

	public void addSelectedNodesItem()
	{
		this.hasSelectedNodesItem = true;
	}

	public void setSelectedNodes()
	{
		setSelected( SELECTED_NODES );
	}

	private void clicked()
	{
		JPopupMenu menu = new JPopupMenu();
		addStringMenu( menu );
		if ( hasEntireGraphItem || hasSelectedNodesItem )
			menu.add( new JSeparator() );
		addLineagesMenu(menu);
		addTagMenu(menu);
		menu.show( this, 0, getHeight() );
	}

	private void addStringMenu( JPopupMenu menu )
	{
		if ( hasEntireGraphItem )
		{
			JMenuItem item = new JMenuItem( ENTIRE_GRAPH );
			item.addActionListener( ignore -> setSelected( ENTIRE_GRAPH ) );
			menu.add( item );
		}
		if ( hasSelectedNodesItem )
		{
			JMenuItem item = new JMenuItem( SELECTED_NODES );
			item.addActionListener( ignore -> setSelected( SELECTED_NODES ) );
			menu.add( item );
		}
	}

	private void addTagMenu(JPopupMenu menu)
	{
		menu.add(new JSeparator());
		menu.add(new JLabel("Tag:"));
		menu.add(new JSeparator());
		TagSetStructure tagSetStructure = mastodonModel.getTagSetModel().getTagSetStructure();
		for ( TagSetStructure.TagSet tagSet : tagSetStructure.getTagSets() )
			menu.add( createTagSetMenu( tagSet ) );
	}

	private void addLineagesMenu( JPopupMenu parentMenu )
	{
		parentMenu.add(new JSeparator());
		parentMenu.add(new JLabel("Lineage:"));
		parentMenu.add(new JSeparator());
		List<Spot> roots = getRoots();
		roots.sort( Comparator.comparing( Spot::getLabel ) );
		if(roots.size() < 10)
		{
			for ( Spot root : roots )
				parentMenu.add( createLineageMenuItem( root ) );
		}
		else
			addSubdividedLineageMenu( parentMenu, roots );
	}

	private void addSubdividedLineageMenu( JPopupMenu popupMenu, List<Spot> roots )
	{
		List<Spot> other = new ArrayList<>();
		Map<Character, List<Spot>> map = new LinkedHashMap<>();
		char[] alphabet = "0123456789".toCharArray();
		for(char key: alphabet)
			map.put( key, new ArrayList<>() );
		for(Spot root : roots) {
			Character character = root.getLabel().charAt( 0 );
			if(map.containsKey( character ))
				map.get( character ).add( root );
			else
				other.add( root );
		}
		map.forEach( (key, list) -> addLineageSubMenu( popupMenu, "\"" + key + "...\"", list ) );
		addLineageSubMenu( popupMenu, "\"A-Z...\"", other );
	}

	private void addLineageSubMenu( JPopupMenu parentMenu, String title, List<Spot> list )
	{
		if ( list.isEmpty() )
			return;
		JMenu menu = new JMenu( title );
		for(Spot root : list )
			menu.add( createLineageMenuItem( root ) );
		parentMenu.add( menu );
	}

	private JMenuItem createLineageMenuItem( Spot root )
	{
		JMenuItem item = new JMenuItem( root.getLabel() );
		item.addActionListener( ignore -> setSelected( root ) );
		return item;
	}

	private List<Spot> getRoots()
	{
		ModelGraph graph = mastodonModel.getGraph();
		List<Spot> roots = new ArrayList<>();
		for ( Spot spot : graph.vertices() )
			if ( spot.incomingEdges().size() == 0 )
			{
				Spot ref = graph.vertexRef().refTo( spot );
				roots.add( ref );
			}
		return roots;
	}

	private JMenu createTagSetMenu( TagSetStructure.TagSet tagSet )
	{
		JMenu menu = new JMenu( tagSet.getName() );
		for ( TagSetStructure.Tag tag : tagSet.getTags() )
		{
			JMenuItem menuItem = new JMenuItem( tag.label() );
			menuItem.setIcon( createIcon( new Color( tag.color() ) ) );
			menuItem.addActionListener( ignore -> setSelected( tag ) );
			menu.add( menuItem );
		}
		return menu;
	}

	private void setSelected( Object value )
	{
		if ( value instanceof TagSetStructure.Tag )
		{
			selected = value;
			TagSetStructure.Tag tag = ( TagSetStructure.Tag ) value;
			setText( tag.label() );
			setIcon( createIcon( new Color( tag.color() ) ) );
		}
		if ( value instanceof Spot )
		{
			selected = value;
			Spot root = ( Spot ) value;
			setText( root.getLabel() );
			setIcon( null );
		}
		if ( ENTIRE_GRAPH.equals( value ) )
		{
			selected = value;
			setText( ENTIRE_GRAPH );
			setIcon( null );
		}
		if ( SELECTED_NODES.equals( value ) )
		{
			selected = value;
			setText( SELECTED_NODES );
			setIcon( null );
		}
	}

	public static ImageIcon createIcon( final Color color )
	{
		final BufferedImage image = new BufferedImage( 20, 20, BufferedImage.TYPE_INT_RGB );
		final Graphics g = image.getGraphics();
		g.setColor( color );
		g.fillRect( 0, 0, image.getWidth(), image.getHeight() );
		g.dispose();
		return new ImageIcon( image );
	}

	public Collection<Spot> getSelectedSpots()
	{
		if ( selected instanceof TagSetStructure.Tag )
			return getSpotsForTag( ( TagSetStructure.Tag ) selected );
		if ( selected instanceof Spot )
			return getAllDescendants( mastodonModel.getGraph(), ( Spot ) selected );
		if ( ENTIRE_GRAPH.equals( selected ) )
			return mastodonModel.getGraph().vertices();
		if ( SELECTED_NODES.equals( selected ) )
			return appModel.getSelectionModel().getSelectedVertices();
		return Collections.emptyList();
	}

	private Collection<Spot> getSpotsForTag( TagSetStructure.Tag tag )
	{
		return mastodonModel.getTagSetModel().getVertexTags().getTaggedWith( tag );
	}

	private static RefSet<Spot> getAllDescendants( ModelGraph graph, Spot root )
	{
		RefSet<Spot> lineage = new RefSetImp<>( graph.vertices().getRefPool() );
		addAllDescendants( graph, root, lineage );
		return lineage;
	}

	private static void addAllDescendants( ModelGraph graph, Spot root, RefSet<Spot> set )
	{
		if ( !set.add( root ) )
			return; // this is only triggered it there is a loop in the graph
		for ( Link edge : root.outgoingEdges() )
		{
			Spot child = edge.getTarget();
			addAllDescendants( graph, child, set );
			graph.releaseRef( child );
		}
	}
}
