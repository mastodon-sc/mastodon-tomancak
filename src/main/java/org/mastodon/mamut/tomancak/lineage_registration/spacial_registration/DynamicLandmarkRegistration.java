package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.RefMapUtils;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

import mpicbg.models.Point;
import mpicbg.models.PointMatch;

public class DynamicLandmarkRegistration implements SpacialRegistration
{

	private static int averageBefore = 2;

	private static int averageAfter = 2;

	private static int averageCount = averageBefore + 1 + averageAfter;

	private final Map< List< double[] >, List< double[] > > landmarks;

	public DynamicLandmarkRegistration( Model modelA, Model modelB, RefRefMap< Spot, Spot > rootsAB )
	{
		int numTimepointsA = computeNumberOfTimepoints( modelA.getGraph() );
		int numTimepointsB = computeNumberOfTimepoints( modelB.getGraph() );
		landmarks = new HashMap<>();
		RefMapUtils.forEach( rootsAB, ( rootA, rootB ) -> {
			List< double[] > landmarkA = computeLandmark( modelA.getGraph(), rootA, numTimepointsA );
			List< double[] > landmarkB = computeLandmark( modelB.getGraph(), rootB, numTimepointsB );
			landmarks.put( landmarkA, landmarkB );
		} );
	}

	private int computeNumberOfTimepoints( ModelGraph graph )
	{
		int max = -1;
		for ( Spot spot : graph.vertices() )
			max = Math.max( max, spot.getTimepoint() );
		return max + 1;
	}

	private List< double[] > computeLandmark( ModelGraph graph, Spot spot, int numTimepoints )
	{
		Collection< Spot > descendants = getDescendants( graph, spot );
		return SortTreeUtils.calculateAndInterpolateAveragePosition( numTimepoints, descendants );
	}

	private Collection< Spot > getDescendants( ModelGraph graph, Spot spot )
	{
		RefSet< Spot > descendants = new RefSetImp<>( graph.vertices().getRefPool() );
		Iterator< Spot > iterator = new DepthFirstIterator<>( spot, graph );
		while ( iterator.hasNext() )
			descendants.add( iterator.next() );
		return descendants;
	}

	@Override
	public AffineTransform3D getTransformationAtoB( int timepointA, int timepointB )
	{
		List< PointMatch > matches = new ArrayList<>();
		landmarks.forEach( ( landmarkA, landmarkB ) -> {
			Point pointA = new Point( average( landmarkA, timepointA ) );
			Point pointB = new Point( average( landmarkB, timepointB ) );
			matches.add( new PointMatch( pointA, pointB ) );
		} );
		return EstimateTransformation.fitTransform( matches );
	}

	private static double[] average( List< double[] > list, int i )
	{
		double[] array = new double[ 3 ];
		for ( int j = i - averageBefore; j <= i + averageAfter; j++ )
			LinAlgHelpers.add( array, get( list, j ), array );
		SortTreeUtils.divide( array, averageCount );
		return array;
	}

	private static double[] get( List< double[] > list, int j )
	{
		if ( j < 0 )
			return list.get( 0 );
		if ( j >= list.size() )
			return list.get( list.size() - 1 );
		return list.get( j );
	}
}
