package org.mastodon.mamut.tomancak.resolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;

import net.imglib2.util.LinAlgHelpers;

import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.graph.ref.OutgoingEdges;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class FuseSpots
{

	public static void run( final ProjectModel projectModel ) {
		final Model model = projectModel.getModel();
		final ModelGraph graph = projectModel.getModel().getGraph();
		final RefSet< Spot > spots = projectModel.getSelectionModel().getSelectedVertices();

		if ( spots.isEmpty() )
		{
			JOptionPane.showMessageDialog( null, "Please select at least two spots to fuse." );
			return;
		}

		try
		{
			final ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
			lock.lock();
			try
			{
				final Spot focus = projectModel.getFocusModel().getFocusedVertex( graph.vertexRef() );
				run( model, spots, focus );
				model.setUndoPoint();
			}
			finally
			{
				lock.unlock();
			}
			graph.notifyGraphChanged();
		}
		catch ( final FuseSpotSelectionException e )
		{
			JOptionPane.showMessageDialog( null,
					"The selection of spots does not fulfill the requirement for \"fuse spots\".\n\n"
							+ "Make sure to only select branches with no division and no merging events.\n"
							+ "All selected branches must have the same number of spots and the same timepoints.",
					"Fuse Spots: Invalid Selection",
					JOptionPane.ERROR_MESSAGE );
		}
	}

	public static void run( final Model model, final Collection< Spot > spots, final Spot focus )
	{
		final ModelGraph graph = model.getGraph();

		final List< RefList< Spot > > tracks = extractTracks( graph, spots );
		RefList< Spot > focusedTrack = focus == null ? tracks.get( 0 ) : findFocusedTrack( tracks, focus );
		focusedTrack = focusedTrack == null ? tracks.get( 0 ) : focusedTrack;
		final ArrayList< RefList< Spot > > nonFocusedTracks = new ArrayList<>( tracks );
		nonFocusedTracks.remove( focusedTrack );

		averageAll( focusedTrack, tracks );
		joinEdges( model, focusedTrack, nonFocusedTracks );
		deleteNonFocusedTracks( graph, nonFocusedTracks );
	}

	private static void joinEdges( final Model model, final RefList< Spot > focusedTrack, final ArrayList< RefList< Spot > > nonFocusedTracks )
	{
		final int max = focusedTrack.size() - 1;
		final Spot ref1 = focusedTrack.createRef();
		final Spot ref2 = focusedTrack.createRef();
		for ( final RefList< Spot > track : nonFocusedTracks )
		{
			copyIncomingEdges( model, track.get( 0, ref2 ), focusedTrack.get( 0, ref1 ) );
			copyOutgoingEdges( model, track.get( max, ref2 ), focusedTrack.get( max, ref1 ) );
		}
	}

	private static void copyIncomingEdges( final Model model, final Spot fromSpot, final Spot toSpot )
	{
		final ModelGraph graph = model.getGraph();
		final Spot ref = graph.vertexRef();
		for ( final Link edge : fromSpot.incomingEdges() )
			copyEdge( model, edge, edge.getSource( ref ), toSpot );
		graph.releaseRef( ref );
	}

	private static void copyOutgoingEdges( final Model model, final Spot fromSpot, final Spot toSpot )
	{
		final ModelGraph graph = model.getGraph();
		final Spot ref = graph.vertexRef();
		for ( final Link edge : fromSpot.outgoingEdges() )
			copyEdge( model, edge, toSpot, edge.getTarget( ref ) );
		graph.releaseRef( ref );
	}

	private static void copyEdge( final Model model, final Link edge, final Spot source, final Spot target )
	{
		final ModelGraph graph = model.getGraph();
		final Link eref = graph.edgeRef();
		try
		{
			if ( graph.getEdge( source, target, eref ) != null )
				return;
			final Link newEdge = graph.addEdge( source, target, eref ).init();
			copyTags( model, edge, newEdge );
		}
		finally
		{
			graph.releaseRef( eref );
		}
	}

	private static void copyTags( final Model model, final Link oldEdge, final Link newEdge )
	{
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final ObjTags< Link > edgeTags = tagSetModel.getEdgeTags();
		for ( final TagSetStructure.TagSet tagSet : tagSetModel.getTagSetStructure().getTagSets() )
		{
			final TagSetStructure.Tag tag = edgeTags.tags( tagSet ).get( oldEdge );
			edgeTags.tags( tagSet ).set( newEdge, tag );
		}
	}

	private static List< RefList< Spot > > extractTracks( final ModelGraph graph, final Collection< Spot > spots )
	{
		final int minTimepoint = spots.stream().mapToInt( Spot::getTimepoint ).min().orElse( Integer.MAX_VALUE );
		final int maxTimepoint = spots.stream().mapToInt( Spot::getTimepoint ).max().orElse( Integer.MIN_VALUE );

		final RefList< Spot > starts = new RefArrayList<>( graph.vertices().getRefPool() );
		for ( final Spot spot : spots )
		{
			if ( spot.getTimepoint() == minTimepoint )
				starts.add( spot );
		}

		final List< RefList< Spot > > tracks = new ArrayList<>();
		for ( final Spot start : starts )
			tracks.add( extractTrack( graph, spots, start, minTimepoint, maxTimepoint ) );
		return tracks;
	}

	private static void deleteNonFocusedTracks( final ModelGraph graph, final ArrayList< RefList< Spot > > nonFocusedTracks )
	{
		for ( final RefList< Spot > track : nonFocusedTracks )
			for ( final Spot spot : track )
				graph.remove( spot );
	}

	private static RefList< Spot > extractTrack( final ModelGraph graph, final Collection< Spot > spots, final Spot start, final int minTimepoint, final int maxTimepoint )
	{
		final Spot ref = graph.vertexRef();
		final RefList< Spot > track = new RefArrayList<>( graph.vertices().getRefPool() );
		track.add( start );
		Spot current = start;
		for ( int t = minTimepoint + 1; t <= maxTimepoint; ++t )
		{
			final OutgoingEdges< Link > outgoingEdges = current.outgoingEdges();
			if ( outgoingEdges.size() != 1 )
				throwException();
			current = outgoingEdges.iterator().next().getTarget( ref );
			if ( current.incomingEdges().size() != 1 || !spots.contains( current ) || current.getTimepoint() != t )
				throwException();
			track.add( current );
		}
		return track;
	}

	private static void throwException()
	{
		throw new FuseSpotSelectionException();
	}

	private static RefList< Spot > findFocusedTrack( final List< RefList< Spot > > tracks, final Spot focus )
	{
		for ( final RefList< Spot > track : tracks )
			if ( track.contains( focus ) )
				return track;
		return null;
	}

	private static void averageAll( final RefList< Spot > focusedTrack, final List< RefList< Spot > > tracks )
	{
		for ( int i = 0; i < focusedTrack.size(); ++i )
			averageTimepoint( i, focusedTrack, tracks );
	}

	private static void averageTimepoint( final int t, final RefList< Spot > focusedTrack, final List< RefList< Spot > > tracks )
	{
		final Spot ref = focusedTrack.createRef();
		try
		{
			final double[] pos = new double[ 3 ];
			final double[] posSum = new double[ 3 ];
			final double[][] cov = new double[ 3 ][ 3 ];
			final double[][] covSum = new double[ 3 ][ 3 ];
			for ( final RefList< Spot > track : tracks )
			{
				final Spot spot = track.get( t, ref );
				spot.localize( pos );
				LinAlgHelpers.add( posSum, pos, posSum );
				spot.getCovariance( cov );
				LinAlgHelpers.add( covSum, cov, covSum );
			}
			LinAlgHelpers.scale( posSum, 1.0 / tracks.size(), pos );
			LinAlgHelpers.scale( covSum, 1.0 / tracks.size(), cov );
			final Spot focus = focusedTrack.get( t, ref );
			focus.setPosition( pos );
			focus.setCovariance( cov );
		}
		finally
		{
			focusedTrack.releaseRef( ref );
		}
	}
}
