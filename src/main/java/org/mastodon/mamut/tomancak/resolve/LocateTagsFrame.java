package org.mastodon.mamut.tomancak.resolve;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphListener;
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

	private final GroupHandle groupHandle;

	private final NavigationHandler< Spot, Link > navigationModel;

	private final FocusModel< Spot > focusModel;

	private final SelectionModel< Spot, Link > selectionModel;

	private final JTable table;

	private final MyTableModel tableModel = new MyTableModel();

	private List< Row > rows = Collections.emptyList();

	private final RefObjectMap< Spot, Row > spotToRow;

	private boolean pauseSelectionNotifications = false;

	public static void run( final ProjectModel projectModel )
	{
		run( projectModel, null );
	}

	public static void run( final ProjectModel projectModel, final TagSetStructure.TagSet tagSet )
	{
		final LocateTagsFrame locateTagsFrame = new LocateTagsFrame( projectModel );
		locateTagsFrame.setSize( 400, 600 );
		locateTagsFrame.setVisible( true );
		if ( tagSet != null )
			locateTagsFrame.setTagSet( tagSet );
	}

	private void setTagSet( final TagSetStructure.TagSet tagSet )
	{
		final int count = tagSetComboBox.getItemCount();
		for ( int i = 0; i < count; i++ )
			if ( tagSet.equals( tagSetComboBox.getItemAt( i ).tagSet ) )
			{
				tagSetComboBox.setSelectedIndex( i );
				break;
			}
	}

	public LocateTagsFrame( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		initializeListeners( projectModel );
		groupHandle = this.projectModel.getGroupManager().createGroupHandle();
		setTitle( "Locate Tags" );
		setLayout( new MigLayout( "insets dialog", "[grow]", "[][][grow]" ) );
		add( new GroupLocksPanel( groupHandle ), "split" );
		add( new Label( "Tag Set:" ) );
		tagSetComboBox = new JComboBox<>();
		add( tagSetComboBox, "grow, wrap, wmin 0" );
		final JButton updateButton = new JButton( "update" );
		updateButton.addActionListener( e -> SwingUtilities.invokeLater( this::fillList ) );
		add( updateButton, "span, split" );
		final JButton removeTagButton = new JButton( "remove selected tags" );
		removeTagButton.addActionListener( e -> removeSelectedTagsClicked( projectModel ) );
		add( removeTagButton, "wrap" );
		table = new JTable();
		table.setAutoCreateRowSorter( true );
		table.getSelectionModel().addListSelectionListener( e -> onSpotItemSelectionChanged() );
		table.setModel( tableModel );
		table.getColumnModel().getColumn( 2 ).setWidth( 20 );
		table.getColumnModel().getColumn( 2 ).setMaxWidth( 20 );
		table.setDefaultRenderer( Color.class, new ColorRenderer() );
		final JScrollPane scrollPane = new JScrollPane( table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		add( scrollPane, "span, grow" );
		fillTagSetComboBox();
		tagSetComboBox.addActionListener( e -> SwingUtilities.invokeLater( this::fillList ) );
		navigationModel = groupHandle.getModel( this.projectModel.NAVIGATION );
		focusModel = this.projectModel.getFocusModel();
		selectionModel = this.projectModel.getSelectionModel();
		spotToRow = new RefObjectHashMap<>( projectModel.getModel().getGraph().vertices().getRefPool() );
		fillList();
	}

	private void removeSelectedTagsClicked( final ProjectModel projectModel )
	{
		final TagSetItem selectedItem = ( TagSetItem ) tagSetComboBox.getSelectedItem();
		if ( selectedItem == null )
			return;
		final Collection< Spot > selectedVertices = getSpotSelectedInTable();
		if ( selectedVertices.isEmpty() )
			return;
		RemoveTagComponents.run( projectModel, selectedItem.tagSet, selectedVertices );
	}

	private void initializeListeners( final ProjectModel projectModel )
	{
		final CloseListener projectCloseListener = this::dispose;
		final MyGraphListener graphListener = new MyGraphListener();
		final GraphChangeListener graphChangeListener = () -> SwingUtilities.invokeLater( this::fillList );
		final TagSetModel.TagSetModelListener tagSetListener = () -> SwingUtilities.invokeLater( this::fillTagSetComboBox );
		projectModel.projectClosedListeners().add( projectCloseListener );
		projectModel.getModel().getGraph().addGraphListener( graphListener );
		projectModel.getModel().getGraph().addGraphChangeListener( graphChangeListener );
		projectModel.getModel().getTagSetModel().listeners().add( tagSetListener );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				projectModel.projectClosedListeners().remove( projectCloseListener );
				projectModel.getModel().getGraph().removeGraphListener( graphListener );
				projectModel.getModel().getGraph().removeGraphChangeListener( graphChangeListener );
				projectModel.getModel().getTagSetModel().listeners().remove( tagSetListener );
			}
		} );
	}

	private void fillList()
	{
		final Model model = projectModel.getModel();
		final TagSetItem item = ( TagSetItem ) tagSetComboBox.getSelectedItem();
		this.rows = item == null ? Collections.emptyList() : getRows( model, item.tagSet );
		updateSpotToRows( this.rows );
		tableModel.fireChange();
	}

	private static List< Row > getRows( final Model model, final TagSetStructure.TagSet tagSet )
	{
		final ModelGraph graph = model.getGraph();
		final RefSet< Spot > roots = RootFinder.getRoots( graph );
		final DepthFirstIterator< Spot, Link > depthFirstIterator = new DepthFirstIterator<>( graph );
		final ObjTagMap< Spot, TagSetStructure.Tag > spotToTag = model.getTagSetModel().getVertexTags().tags( tagSet );
		final Spot ref = graph.vertexRef();
		final List< Row > rows = new ArrayList<>();
		for ( final Spot root : roots )
		{
			depthFirstIterator.reset( root );
			while ( depthFirstIterator.hasNext() )
			{
				final Spot spot = depthFirstIterator.next();
				final TagSetStructure.Tag tag = spotToTag.get( spot );
				if ( !entryPoint( spotToTag, spot, tag, ref ) )
					continue;
				rows.add( new Row( graph, tag, spot, root.getLabel() ) );
			}
		}
		// TODO fix sorting
		rows.sort( Comparator.comparing( Row::toString ) );
		return rows;
	}

	private void updateSpotToRows( final List< Row > rows )
	{
		spotToRow.clear();
		for ( final Row row : rows )
			spotToRow.put( row.spot, row );
	}


	private class MyGraphListener implements GraphListener< Spot, Link >
	{
		@Override
		public void graphRebuilt()
		{
			SwingUtilities.invokeLater( LocateTagsFrame.this::fillList );
		}

		@Override
		public void vertexAdded( final Spot spot )
		{

		}

		@Override
		public void vertexRemoved( final Spot spot )
		{
			final Row row = spotToRow.get( spot );
			if ( row != null )
			{
				rows.remove( row );
				spotToRow.remove( spot );
				tableModel.fireChange();
			}
		}

		@Override
		public void edgeAdded( final Link link )
		{

		}

		@Override
		public void edgeRemoved( final Link link )
		{

		}
	}


	private class MyTableModel implements TableModel
	{

		private final List< TableModelListener > listeners = new CopyOnWriteArrayList<>();

		private final List< String > columns = Arrays.asList( "time point", "tag", "", "track", "spot" );

		private final List< Class< ? > > classes = Arrays.asList( Integer.class, String.class, Color.class, String.class, String.class );

		@Override
		public int getRowCount()
		{
			return rows.size();
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
			final Row row = rows.get( rowIndex );
			switch ( columnIndex )
			{
			case 0:
				return row.spot.getTimepoint();
			case 1:
				return row.tag.label();
			case 2:
				return new Color( row.tag.color() );
			case 3:
				return row.root;
			case 4:
				return row.spot.getLabel();
			}
			return null;
		}

		@Override
		public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex )
		{

		}

		public void fireChange()
		{
			pauseSelectionNotifications = true;
			try
			{
				final TableModelEvent e = new TableModelEvent( this );
				for ( final TableModelListener listener : listeners )
					listener.tableChanged( e );
			}
			finally
			{
				pauseSelectionNotifications = false;
			}
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

	private static boolean entryPoint( final ObjTagMap< Spot, TagSetStructure.Tag > spotToTag, final Spot spot, final TagSetStructure.Tag tag, final Spot ref )
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
		final TagSetItem selectedItem = ( TagSetItem ) tagSetComboBox.getSelectedItem();
		tagSetComboBox.removeAllItems();
		for ( final TagSetStructure.TagSet tagSet : tagSets )
			tagSetComboBox.addItem( new TagSetItem( tagSet ) );
		if ( selectedItem != null )
			tagSetComboBox.setSelectedItem( selectedItem );
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

		@Override
		public int hashCode()
		{
			return tagSet.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if ( obj instanceof TagSetItem )
				return tagSet.equals( ( ( TagSetItem ) obj ).tagSet );
			return false;
		}
	}

	private static class Row
	{

		private final TagSetStructure.Tag tag;

		private final Spot spot;

		private final String root;

		public Row( final ModelGraph graph, final TagSetStructure.Tag tag, final Spot spot, final String root )
		{
			this.tag = Objects.requireNonNull( tag );
			this.spot = graph.vertexRef();
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
		if ( pauseSelectionNotifications )
			return;
		updateLeadSelection();
		updateMultiSelection();
	}

	private void updateMultiSelection()
	{
		final Collection< Spot > spots = getSpotSelectedInTable();
		if ( spots.isEmpty() )
			return;
		selectionModel.clearSelection();
		selectionModel.setVerticesSelected( spots, true );
	}

	private Collection< Spot > getSpotSelectedInTable()
	{
		final ListSelectionModel selection = table.getSelectionModel();
		final int min = selection.getMinSelectionIndex();
		final int max = selection.getMaxSelectionIndex();
		if ( min < 0 || max < 0 )
			return Collections.emptyList();

		final RefList< Spot > spots = RefCollections.createRefList( projectModel.getModel().getGraph().vertices() );
		for ( int i = min; i <= max; i++ )
			if ( selection.isSelectedIndex( i ) )
				spots.add( rows.get( table.convertRowIndexToModel( i ) ).spot );
		return spots;
	}

	private void updateLeadSelection()
	{
		try
		{
			final ListSelectionModel selection = table.getSelectionModel();
			final int leadSelectionIndex = selection.getLeadSelectionIndex();
			if ( leadSelectionIndex < 0 )
				return;
			final Row row = rows.get( table.convertRowIndexToModel( leadSelectionIndex ) );
			groupHandle.setGroupId( 0 );
			navigationModel.notifyNavigateToVertex( row.spot );
			focusModel.focusVertex( row.spot );
			selectionModel.clearSelection();
		}
		catch ( final IndexOutOfBoundsException e )
		{
			// ignore
		}
	}
}
