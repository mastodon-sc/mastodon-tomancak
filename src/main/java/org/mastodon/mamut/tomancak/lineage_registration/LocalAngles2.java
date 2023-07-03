package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.EstimateTransformation;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;
import org.mastodon.util.DepthFirstIteration;

import mpicbg.models.Point;
import mpicbg.models.PointMatch;

public class LocalAngles2
{

	private RefObjectMap< Spot, double[][] > landmarksMapA;

	private RefObjectMap< Spot, double[][] > landmarksMapB;

	public static List< Pair< Double, Double > > getLocalAngles( RegisteredGraphs rg )
	{
		ModelGraph graphA = rg.graphA;
		ModelGraph graphB = rg.graphB;
		LocalAngles2 localAngles2 = new LocalAngles2( graphA, graphB );
		ArrayList< Pair< Double, Double > > pairs = new ArrayList<>();
		RefMapUtils.forEach( rg.mapAB, ( branchStartA, branchStartB ) -> {
			double angle = localAngles2.computeAngle( branchStartA, branchStartB );
			if ( Double.isNaN( angle ) )
				return;
			int timepointA = endTimepoint( graphA, branchStartA );
			pairs.add( new ValuePair<>( ( double ) timepointA, angle ) );
		} );
		return pairs;
	}

	private final ModelGraph graphA;

	private final ModelGraph graphB;

	public LocalAngles2( ModelGraph graphA, ModelGraph graphB )
	{
		this.graphA = graphA;
		this.graphB = graphB;
		RefSet< Spot > branchStartsA = BranchGraphUtils.getAllBranchStarts( graphA );
		RefSet< Spot > branchStartsB = BranchGraphUtils.getAllBranchStarts( graphB );
		landmarksMapA = getLandmarksMap( graphA, branchStartsA );
		landmarksMapB = getLandmarksMap( graphB, branchStartsB );
	}

	public double computeAngle( Spot branchStartA, Spot branchStartB )
	{
		try
		{
			double[][] landmarksA = landmarksMapA.get( branchStartA );
			double[][] landmarksB = landmarksMapB.get( branchStartB );
			double[] directionA = cellDivisionDirection( graphA, branchStartA );
			double[] directionB = cellDivisionDirection( graphB, branchStartB );
			return computeAngle( directionA, landmarksA, directionB, landmarksB );
		}
		catch ( NullPointerException exception )
		{
			return Double.NaN;
		}
	}

	private static double computeAngle( double[] directionA, double[][] landmarksA, double[] directionB, double[][] landmarksB )
	{
		List< PointMatch > matches = new ArrayList<>();
		for ( int i = 0; i < landmarksA.length; i++ )
			matches.add( new PointMatch( new Point( landmarksA[ i ] ), new Point( landmarksB[ i ] ) ) );
		AffineTransform3D tranformAB = EstimateTransformation.fitTransform( matches );
		double[] target = new double[ 3 ];
		tranformAB.setTranslation( 0, 0, 0 );
		tranformAB.applyInverse( target, directionB );
		return SortTreeUtils.angleInDegree( directionA, target );
	}

	private static double[] cellDivisionDirection( ModelGraph graph, Spot branchStart )
	{
		Spot ref = graph.vertexRef();
		try
		{
			Spot branchEnd = BranchGraphUtils.getBranchEnd( branchStart, ref );
			return SortTreeUtils.directionOfCellDevision( graph, branchEnd );
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}

	private static RefObjectMap< Spot, double[][] > getLandmarksMap( ModelGraph graph, RefSet< Spot > branchStarts )
	{
		RefRefMap< Spot, Spot > parentMap = getParentMap( graph, branchStarts );
		RefRefMap< Spot, Spot > siblingMap = getSiblingsMap( graph, branchStarts );
		RefRefMap< Spot, Spot > auntMap = concat( graph, parentMap, siblingMap );
		RefRefMap< Spot, Spot > grandAuntMap = concat( graph, parentMap, auntMap );
		RefRefMap< Spot, Spot > ggAuntMap = concat( graph, parentMap, grandAuntMap );
		RefRefMap< Spot, Spot > gggAuntMap = concat( graph, parentMap, ggAuntMap );
		Spot ref = graph.vertices().createRef();
		try
		{
			RefObjectMap< Spot, double[][] > landmarks = new RefObjectHashMap<>( graph.vertices().getRefPool() );
			for ( Spot spot : branchStarts )
			{
				try
				{
					int timepoint = endTimepoint( graph, spot );
					int divisionTime = timepoint + 2;
					double[] spotPosition = getDescendantsPosition( graph, spot, divisionTime );
					double[] siblingsPosition = getDescendantsPosition( graph, siblingMap.get( spot, ref ), divisionTime );
					double[] auntsPosition = getDescendantsPosition( graph, auntMap.get( spot, ref ), divisionTime );
					// NB: The next line is there just to creat a NullPointerException if gen < 2.
					// Using the new method to compute the angle early on, gives slightly worse angles.
					double[] grantAuntsPosition = getDescendantsPosition( graph, grandAuntMap.get( spot, ref ), divisionTime );
//					double[] ggAuntsPosition = getDescendantsPosition( graph, ggAuntMap.get( spot, ref ), divisionTime );
//					double[] gggAuntsPosition = getDescendantsPosition( graph, gggAuntMap.get( spot, ref ), divisionTime );
					landmarks.put( spot, new double[][] { spotPosition, siblingsPosition, auntsPosition, grantAuntsPosition } );
				}
				catch ( NullPointerException e )
				{
					// ignore
				}
			}
			return landmarks;
		}
		finally
		{
			graph.vertices().releaseRef( ref );
		}
	}

	private static int endTimepoint( ModelGraph graph, Spot spot )
	{
		Spot ref = graph.vertices().createRef();
		try
		{
			return BranchGraphUtils.getBranchEnd( spot, ref ).getTimepoint();
		}
		finally
		{
			graph.vertices().releaseRef( ref );
		}
	}

	private static double[] getDescendantsPosition( ModelGraph graph, Spot branchEnd, int divisionTime )
	{
		double[] sum = new double[] { 0, 0, 0 };
		int count = 0;
		for ( DepthFirstIteration.Step< Spot > step : DepthFirstIteration.forRoot( graph, branchEnd ) )
		{
			Spot node = step.node();
			if ( node.getTimepoint() > divisionTime + 1 )
			{
				step.truncate();
				continue;
			}
			if ( !step.isFirstVisit() )
				continue;

			if ( node.getTimepoint() >= divisionTime - 1 )
			{
				for ( int d = 0; d < 3; d++ )
					sum[ d ] += node.getDoublePosition( d );
				count++;
			}
		}
		if ( count == 0 )
			throw new NullPointerException( "No descendants found." );
		LinAlgHelpers.scale( sum, 1.0 / count, sum );
		return sum;
	}

	private static RefRefMap< Spot, Spot > concat( ModelGraph graph, RefRefMap< Spot, Spot > map1, RefRefMap< Spot, Spot > map2 )
	{
		Spot ref1 = graph.vertices().createRef();
		Spot ref2 = graph.vertices().createRef();
		try
		{
			RefRefHashMap< Spot, Spot > map = new RefRefHashMap<>( graph.vertices().getRefPool(), graph.vertices().getRefPool() );
			for ( Spot x : map1.keySet() )
			{
				Spot y = map1.get( x, ref1 );
				Spot z = map2.get( y, ref2 );
				if ( z != null )
					map.put( x, z );
			}
			return map;
		}
		finally
		{
			graph.vertices().releaseRef( ref1 );
			graph.vertices().releaseRef( ref2 );
		}
	}

	private static RefRefMap< Spot, Spot > getSiblingsMap( ModelGraph graph, RefSet< Spot > branchStarts )
	{
		RefRefHashMap< Spot, Spot > map = new RefRefHashMap<>( graph.vertices().getRefPool(), graph.vertices().getRefPool() );
		Spot ref1 = graph.vertices().createRef();
		Spot ref2 = graph.vertices().createRef();
		Spot ref3 = graph.vertices().createRef();
		for ( Spot branchStart : branchStarts )
		{
			if ( !isDividing( graph, branchStart ) )
				continue;
			Spot branchEnd = BranchGraphUtils.getBranchEnd( branchStart, ref1 );
			Spot sibling1 = branchEnd.outgoingEdges().get( 0 ).getTarget( ref2 );
			Spot sibling2 = branchEnd.outgoingEdges().get( 1 ).getTarget( ref3 );
			map.put( sibling1, sibling2 );
			map.put( sibling2, sibling1 );
		}
		return map;
	}

	private static boolean isDividing( ModelGraph graph, Spot branchStart )
	{
		Spot ref = graph.vertices().createRef();
		try
		{
			Spot branchEnd = BranchGraphUtils.getBranchEnd( branchStart, ref );
			return branchEnd.outgoingEdges().size() == 2;
		}
		finally
		{
			graph.vertices().releaseRef( ref );
		}
	}

	private static RefRefMap< Spot, Spot > getParentMap( ModelGraph graph, RefSet< Spot > branchStarts )
	{
		RefRefMap< Spot, Spot > parentMap = new RefRefHashMap<>( graph.vertices().getRefPool(), graph.vertices().getRefPool() );
		Spot ref = graph.vertices().createRef();
		Spot ref2 = graph.vertices().createRef();
		for ( Spot branchStart : branchStarts )
		{
			if ( branchStart.incomingEdges().size() != 1 )
				continue;
			Spot parent = branchStart.incomingEdges().iterator().next().getSource( ref );
			Spot parentStart = BranchGraphUtils.getBranchStart( parent, ref2 );
			parentMap.put( branchStart, parentStart );
		}
		return parentMap;
	}
}
