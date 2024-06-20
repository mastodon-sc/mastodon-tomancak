package org.mastodon.mamut.tomancak.resolve;

import java.awt.Dimension;
import java.awt.Label;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.graph.ref.IncomingEdges;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class LocateTagsFrame extends JFrame
{

	private final JComboBox< TagSetItem > tagSetComboBox;

	private final ProjectModel projectModel;

	private final JList< SpotItem > list;

	public LocateTagsFrame( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
		setTitle( "Locate Tags" );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setLayout( new MigLayout( "insets dialog", "[][grow]", "[][grow]" ) );
		add( new Label( "Tag Set:" ) );
		tagSetComboBox = new JComboBox<>();
		add( tagSetComboBox, "grow, wrap" );
		list = new JList<>();
		list.setPreferredSize( new Dimension( 200, 200 ) );
		add( new JScrollPane( list ), "span, grow" );
		fillTagSetComboBox();
		tagSetComboBox.addActionListener( e -> fillList() );
	}

	public static void run( final ProjectModel pluginAppModel )
	{
		final LocateTagsFrame locateTagsFrame = new LocateTagsFrame( pluginAppModel );
		locateTagsFrame.setSize( 400, 600 );
		locateTagsFrame.setVisible( true );
	}

	private void fillList()
	{
		list.removeAll();
		final Model model = projectModel.getModel();
		final ModelGraph graph = model.getGraph();
		// iterate over the graph in trackscheme order
		final TagSetItem item = ( TagSetItem ) tagSetComboBox.getSelectedItem();
		final RefSet< Spot > roots = RootFinder.getRoots( graph );
		final DepthFirstIterator< Spot, Link > depthFirstIterator = new DepthFirstIterator<>( graph );
		final ObjTagMap< Spot, TagSetStructure.Tag > spotToTag = model.getTagSetModel().getVertexTags().tags( item.tagSet );
		final Spot ref = graph.vertexRef();
		final List< SpotItem > items = new ArrayList<>();
		for ( final Spot root : roots )
		{
			depthFirstIterator.reset( root );
			while ( depthFirstIterator.hasNext() )
			{
				final Spot spot = depthFirstIterator.next();
				final TagSetStructure.Tag tag = spotToTag.get( spot );
				if ( !entryPoint( spotToTag, spot, tag, ref ) )
					continue;
				items.add( new SpotItem( tag, spot, root.getLabel() ) );
			}
		}
		items.sort( Comparator.comparing( SpotItem::toString ) );
		DefaultListModel< SpotItem > listModel = new DefaultListModel<>();
		items.forEach( listModel::addElement );
		list.setModel( listModel );
		list.addListSelectionListener( e -> onSpotItemSelectionChanged() );
	}

	private boolean entryPoint( final ObjTagMap< Spot, TagSetStructure.Tag > spotToTag, final Spot spot, final TagSetStructure.Tag tag, final Spot ref )
	{
		if ( tag == null )
			return false;
		final IncomingEdges< Link >.IncomingEdgesIterator links = spot.incomingEdges().iterator();
		if ( !links.hasNext() )
			return true;
		final Spot parent = links.next().getSource( ref );
		final TagSetStructure.Tag parentTag = spotToTag.get( parent );
		return tag != parentTag;
	}

	private void fillTagSetComboBox()
	{
		final Model model = projectModel.getModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final List< TagSetStructure.TagSet > tagSets = tagSetModel.getTagSetStructure().getTagSets();
		for ( final TagSetStructure.TagSet tagSet : tagSets )
			tagSetComboBox.addItem( new TagSetItem( tagSet ) );
	}

	private static class TagSetItem
	{
		private final TagSetStructure.TagSet tagSet;

		public TagSetItem( final TagSetStructure.TagSet tagSet )
		{
			this.tagSet = tagSet;
		}

		@Override
		public String toString()
		{
			return tagSet.getName();
		}
	}

	private class SpotItem
	{

		private final TagSetStructure.Tag tag;

		private final Spot spot;

		private final String root;

		public SpotItem( final TagSetStructure.Tag tag, final Spot spot, final String root )
		{
			this.tag = Objects.requireNonNull( tag );
			this.spot = projectModel.getModel().getGraph().vertices().createRef();
			this.spot.refTo( spot );
			this.root = root;
		}

		@Override
		public String toString()
		{
			return String.format( "time: %d  tag: %s  track: %s  spot: %s", spot.getTimepoint(), tag.label(), root, spot.getLabel() );
		}
	}

	private void onSpotItemSelectionChanged()
	{
		final SpotItem item = list.getSelectedValue();
		final GroupHandle groupHandle = projectModel.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		final NavigationHandler< Spot, Link > navigateTo = groupHandle.getModel( projectModel.NAVIGATION );
		navigateTo.notifyNavigateToVertex( item.spot );
		projectModel.getFocusModel().focusVertex( item.spot );
		final SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
		selectionModel.clearSelection();
		for ( final SpotItem item1 : list.getSelectedValuesList() )
			selectionModel.setSelected( item1.spot, true );
	}
}
