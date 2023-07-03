package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.ToDoubleBiFunction;

import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.SortTreeUtils;

public class MeasureRegistrationQuality
{
	public static double measure( RegisteredGraphs rg )
	{
		RefDoubleMap< Spot > labelsA = countLabel( rg.graphA );
		RefDoubleMap< Spot > labelsB = countLabel( rg.graphB );
		ToDoubleBiFunction< Spot, Spot > reward = ( branchA, branchB ) -> {
			double labelA = labelsA.get( branchA );
			double labelB = labelsB.get( branchB );
			return Math.min( labelA, labelB );
		};
		double[] sum = { 0 };
		RefMapUtils.forEach( rg.mapAB, ( branchA, branchB ) -> {
			sum[ 0 ] += reward.applyAsDouble( branchA, branchB );
		} );
		return sum[ 0 ];
	}

	private static RefDoubleMap< Spot > countLabel( ModelGraph graph )
	{
		int[] numberOfCellsPerTimepointA = computeNumberOfCellsPerTimepoint( graph );
		RefDoubleMap< Spot > labels = new RefDoubleHashMap<>( graph.vertices().getRefPool(), Double.NaN );
		Spot ref = graph.vertexRef();
		for ( Spot branchStart : BranchGraphUtils.getAllBranchStarts( graph ) )
		{
			boolean isRoot = branchStart.incomingEdges().isEmpty();
			double label = 0;
			if ( !isRoot )
			{
				Spot branchEnd = BranchGraphUtils.getBranchEnd( branchStart, ref );
				int startCount = numberOfCellsPerTimepointA[ branchStart.getTimepoint() ];
				int endCount = numberOfCellsPerTimepointA[ branchEnd.getTimepoint() ];
				label = Math.max( 0, endCount - startCount );
			}
			labels.put( branchStart, label );
		}
		return labels;
	}

	private static RefSet< Spot > dividingBranches( ModelGraph graph )
	{
		RefSet< Spot > dividing = new RefSetImp<>( graph.vertices().getRefPool(), Integer.MIN_VALUE );
		Spot ref = graph.vertexRef();
		for ( Spot branchStart : BranchGraphUtils.getAllBranchStarts( graph ) )
		{
			Spot branchEnd = BranchGraphUtils.getBranchEnd( branchStart, ref );
			if ( !branchEnd.outgoingEdges().isEmpty() )
				dividing.add( branchEnd );
		}
		return dividing;
	}

	private static int[] computeNumberOfCellsPerTimepoint( ModelGraph graph )
	{
		int numberOfTimepoints = SortTreeUtils.getNumberOfTimePoints( graph );
		int[] values = new int[ numberOfTimepoints ];
		for ( Spot spot : graph.vertices() )
			values[ spot.getTimepoint() ]++;
		for ( int i = 1; i < values.length; i++ )
			values[ i ] = Math.max( values[ i - 1 ], values[ i ] );
		return values;
	}
}
