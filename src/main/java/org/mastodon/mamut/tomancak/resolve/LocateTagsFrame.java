package org.mastodon.mamut.tomancak.resolve;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.graph.ref.IncomingEdges;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.CloseListener;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.FocusModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class LocateTagsFrame extends JFrame
{

	private final JComboBox< TagSetItem > tagSetComboBox;

	private final ProjectModel projectModel;

	private final JTable table;

	private final CloseListener projectCloseListener = this::dispose;

	private final GroupHandle groupHandle;

	private final NavigationHandler< Spot, Link > navigationModel;

	private final FocusModel< Spot > focusModel;

	private final SelectionModel< Spot, Link > selectionModel;

	private final MyTableModel dataModel = new MyTableModel();

	private List< SpotItem > items = Collections.emptyList();

	public LocateTagsFrame( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		initializeListeners( projectModel );
		groupHandle = this.projectModel.getGroupManager().createGroupHandle();
		setTitle( "Locate Tags" );
		setLayout( new MigLayout( "insets dialog", "[grow]", "[][grow]" ) );
		add( new GroupLocksPanel( groupHandle ), "split" );
		add( new Label( "Tag Set:" ) );
		tagSetComboBox = new JComboBox<>();
		add( tagSetComboBox, "grow, wrap, wmin 0" );
		table = new JTable();
		table.setAutoCreateRowSorter( true );
		table.getSelectionModel().addListSelectionListener( e -> onSpotItemSelectionChanged() );
		table.setModel( dataModel );
		table.getColumnModel().getColumn( 2 ).setWidth( 20 );
		table.getColumnModel().getColumn( 2 ).setMaxWidth( 20 );
		table.setDefaultRenderer( Color.class, new ColorRenderer() );
		final JScrollPane scrollPane = new JScrollPane( table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		add( scrollPane, "span, grow" );
		fillTagSetComboBox();
		tagSetComboBox.addActionListener( e -> fillList() );
		navigationModel = groupHandle.getModel( this.projectModel.NAVIGATION );
		focusModel = this.projectModel.getFocusModel();
		selectionModel = this.projectModel.getSelectionModel();
	}

	private void initializeListeners( final ProjectModel projectModel )
	{
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				onClose();
			}
		} );
		projectModel.projectClosedListeners().add( projectCloseListener );
	}

	private void onClose()
	{
		projectModel.projectClosedListeners().remove( projectCloseListener );
	}

	public static void run( final ProjectModel pluginAppModel )
	{
		final LocateTagsFrame locateTagsFrame = new LocateTagsFrame( pluginAppModel );
		locateTagsFrame.setSize( 400, 600 );
		locateTagsFrame.setVisible( true );
	}

	private void fillList()
	{
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
		this.items = items;
		dataModel.fireChange();
	}

	private class MyTableModel implements TableModel
	{

		private final List< TableModelListener > listeners = new CopyOnWriteArrayList<>();

		private final List< String > columns = Arrays.asList( "time point", "tag", "", "track", "spot" );

		private final List< Class< ? > > classes = Arrays.asList( Integer.class, String.class, Color.class, String.class, String.class );

		@Override
		public int getRowCount()
		{
			return items.size();
		}

		@Override
		public int getColumnCount()
		{
			return columns.size();
		}

		@Override
		public String getColumnName( final int columnIndex )
		{
			return columns.get( columnIndex );
		}

		@Override
		public Class< ? > getColumnClass( final int columnIndex )
		{
			return classes.get( columnIndex );
		}

		@Override
		public boolean isCellEditable( final int rowIndex, final int columnIndex )
		{
			return false;
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			final SpotItem item = items.get( rowIndex );
			switch ( columnIndex )
			{
			case 0:
				return item.spot.getTimepoint();
			case 1:
				return item.tag.label();
			case 2:
				return new Color( item.tag.color() );
			case 3:
				return item.root;
			case 4:
				return item.spot.getLabel();
			}
			return null;
		}

		@Override
		public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex )
		{

		}

		public void fireChange()
		{
			final TableModelEvent e = new TableModelEvent( this );
			for ( final TableModelListener listener : listeners )
				listener.tableChanged( e );
		}

		@Override
		public void addTableModelListener( final TableModelListener l )
		{
			listeners.add( l );
		}

		@Override
		public void removeTableModelListener( final TableModelListener l )
		{
			listeners.remove( l );
		}
	}

	private static class ColorRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			final Component component = super.getTableCellRendererComponent( table, "", isSelected, hasFocus, row, column );
			component.setBackground( ( Color ) value );
			return component;
		}
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
		updateLeadSelection();
		updateMultiSelection();
	}

	private void updateMultiSelection()
	{
		final ListSelectionModel selection = table.getSelectionModel();
		final int min = selection.getMinSelectionIndex();
		final int max = selection.getMaxSelectionIndex();
		if ( min < 0 || max < 0 )
			return;

		selectionModel.clearSelection();
		for ( int i = min; i <= max; i++ )
			if ( selection.isSelectedIndex( i ) )
			{
				final Spot spot = items.get( table.convertRowIndexToModel( i ) ).spot;
				selectionModel.setSelected( spot, true );
			}
	}

	private void updateLeadSelection()
	{
		try
		{
			final ListSelectionModel selection = table.getSelectionModel();
			final int leadSelectionIndex = selection.getLeadSelectionIndex();
			if ( leadSelectionIndex < 0 )
				return;
			final SpotItem item = items.get( table.convertRowIndexToModel( leadSelectionIndex ) );
			groupHandle.setGroupId( 0 );
			navigationModel.notifyNavigateToVertex( item.spot );
			focusModel.focusVertex( item.spot );
			selectionModel.clearSelection();
		}
		catch ( final IndexOutOfBoundsException e )
		{
			// ignore
		}
	}
}
