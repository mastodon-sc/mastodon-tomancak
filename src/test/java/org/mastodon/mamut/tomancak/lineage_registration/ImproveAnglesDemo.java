package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistrationMethod;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.scijava.Context;

public class ImproveAnglesDemo
{
	public static void main( String... args )
	{
		try (Context context = new Context())
		{
			WindowManager windowManager1 = LineageRegistrationDemo.openAppModel( context, LineageRegistrationDemo.project1 );
			WindowManager windowManager2 = LineageRegistrationDemo.openAppModel( context, LineageRegistrationDemo.project2 );
			RegisteredGraphs rg = LineageRegistrationAlgorithm.run(
					windowManager1.getAppModel().getModel(), 0,
					windowManager2.getAppModel().getModel(), 0,
					SpatialRegistrationMethod.DYNAMIC_ROOTS );
			LineageRegistrationUtils.plotAngleAgainstTimepoint( rg.anglesA );
			ModelGraph graphA = windowManager1.getAppModel().getModel().getGraph();
			ModelBranchGraph branchGraphA = new ModelBranchGraph( graphA );
			RefObjectMap< BranchSpot, double[] > directionsA = computeDirections( graphA, branchGraphA );
			RefObjectMap< BranchSpot, double[] > parentDirectionsA = fromParentToChild( directionsA, branchGraphA );
			RefObjectMap< BranchSpot, double[] > grantParentDirectionsA = fromParentToChild( parentDirectionsA, branchGraphA );
			ModelGraph graphB = windowManager2.getAppModel().getModel().getGraph();
			ModelBranchGraph branchGraphB = new ModelBranchGraph( graphB );
			RefObjectMap< BranchSpot, double[] > directionsB = computeDirections( graphB, branchGraphB );
			RefObjectMap< BranchSpot, double[] > parentDirectionsB = fromParentToChild( directionsB, branchGraphB );
			RefObjectMap< BranchSpot, double[] > grantParentDirectionsB = fromParentToChild( parentDirectionsB, branchGraphB );
			RefRefMap< BranchSpot, BranchSpot > map = toBranchSpotMap( rg.mapAB, branchGraphA, branchGraphB );
			List< Pair< Double, Double > > angles = new ArrayList<>();
			RefMapUtils.forEach( map, ( a, b ) -> {
				try
				{
					double[] dA = directionsA.get( a );
					double[] dA1 = parentDirectionsA.get( a );
					double[] dA2 = grantParentDirectionsA.get( a );
					double[] dB = directionsB.get( b );
					double[] dB1 = parentDirectionsB.get( b );
					double[] dB2 = grantParentDirectionsB.get( b );
					double angleA = SortTreeUtils.angleInDegree( dA, dA1 );
					double angleB = SortTreeUtils.angleInDegree( dB, dB1 );
					double v = angleA - angleB;
					boolean isFirstChild = isFirstChild( branchGraphA, a );
					if ( isFirstChild )
					{
						angles.add( new ValuePair<>( a.getFirstTimePoint() - 1., v ) );
					}
				}
				catch ( NullPointerException e )
				{
					// ignore (happens when a or b is not in the map)
				}
			} );
			plotAngles( angles );
		}
	}

	private static < T > RefObjectMap< BranchSpot, T > fromParentToChild( RefObjectMap< BranchSpot, T > values, ModelBranchGraph branchGraph )
	{
		BranchSpot ref = branchGraph.vertexRef();
		try
		{
			RefObjectMap< BranchSpot, T > map = new RefObjectHashMap<>( branchGraph.vertices().getRefPool(), values.size() );
			for ( BranchSpot spot : values.keySet() )
				map.put( spot, values.get( getParent( spot, ref ) ) );
			return map;
		}
		finally
		{
			branchGraph.releaseRef( ref );
		}
	}

	private static boolean isFirstChild( ModelBranchGraph graph, BranchSpot spot )
	{
		BranchSpot ref = graph.vertexRef();
		BranchSpot ref2 = graph.vertexRef();
		try
		{
			if ( spot.incomingEdges().size() != 1 )
				return false;
			BranchSpot parent = spot.incomingEdges().iterator().next().getSource( ref );
			BranchSpot firstChild = parent.outgoingEdges().iterator().next().getTarget( ref2 );
			return spot.equals( firstChild );
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
		}
	}

	private static void plotAngles( List< Pair< Double, Double > > angles )
	{
		// Use JFreeChart to plot the angles.
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series = new XYSeries( "Angles" );
		for ( Pair< Double, Double > angle : angles )
			series.add( angle.getA(), angle.getB() );
		dataset.addSeries( series );
		JFreeChart chart = ChartFactory.createScatterPlot( "Angles", "Angle A", "Angle B", dataset );
		ChartFrame frame = new ChartFrame( "Angles", chart );
		frame.pack();
		frame.setVisible( true );
	}

	private static double angleToParentCellDivision( RefObjectMap< BranchSpot, double[] > directionsA, RefObjectMap< BranchSpot, double[] > grantParentDirectionsA, BranchSpot a, BranchSpot refA,
			BranchSpot refA2 )
	{
		double[] dirA = directionsA.get( a );
		if ( dirA == null )
			return Double.NaN;
		if ( !hasParent( a ) )
			return Double.NaN;
		BranchSpot parentA = getParent( a, refA );
//		if ( !hasParent( parentA ) )
//			return Double.NaN;
//		BranchSpot grantParent = getParent( parentA, refA2 );
		double[] pDirA = directionsA.get( parentA );
		double[] pp = grantParentDirectionsA.get( a );
		if ( pp != pDirA )
			throw new AssertionError();
		if ( pDirA == null )
			return Double.NaN;
		return SortTreeUtils.angleInDegree( dirA, pDirA );
	}

	private static BranchSpot getParent( BranchSpot a, BranchSpot refA )
	{
		return a.incomingEdges().get( 0 ).getSource( refA );
	}

	private static boolean hasParent( BranchSpot a )
	{
		return a.incomingEdges().size() == 1;
	}

	private static RefRefMap< BranchSpot, BranchSpot > toBranchSpotMap( RefRefMap< Spot, Spot > mapAB, ModelBranchGraph branchGraphA, ModelBranchGraph branchGraphB )
	{
		BranchSpot refA = branchGraphA.vertexRef();
		BranchSpot refB = branchGraphB.vertexRef();
		try
		{
			RefRefMap< BranchSpot, BranchSpot > map = new RefRefHashMap<>( branchGraphA.vertices().getRefPool(), branchGraphB.vertices().getRefPool() );
			RefMapUtils.forEach( mapAB, ( a, b ) -> {
				BranchSpot branchA = branchGraphA.getBranchVertex( a, refA );
				BranchSpot branchB = branchGraphB.getBranchVertex( b, refB );
				map.put( branchA, branchB );
			} );
			return map;
		}
		finally
		{
			branchGraphA.releaseRef( refA );
			branchGraphB.releaseRef( refB );
		}
	}

	private static RefObjectMap< BranchSpot, double[] > computeDirections( ModelGraph graph, ModelBranchGraph branchGraph )
	{
		RefObjectMap< BranchSpot, double[] > directions = new RefObjectHashMap<>( branchGraph.vertices().getRefPool(), branchGraph.vertices().size() );
		Spot ref = graph.vertexRef();
		BranchSpot ref2 = branchGraph.vertexRef();
		for ( BranchSpot branch : branchGraph.vertices() )
		{
			if ( branch.outgoingEdges().size() != 2 )
				continue;
			double[] direction = SortTreeUtils.directionOfCellDevision( graph, branchGraph.getLastLinkedVertex( branch, ref ) );
			Iterator< BranchLink > iterator = branch.outgoingEdges().iterator();
			BranchSpot child = iterator.next().getTarget( ref2 );
			directions.put( child, direction );
			child = iterator.next().getTarget( ref2 );
			double[] oppositeDirection = new double[ 3 ];
			LinAlgHelpers.scale( direction, -1, oppositeDirection );
			directions.put( child, oppositeDirection );
		}
		return directions;
	}
}
