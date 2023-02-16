package org.mastodon.mamut.tomancak.lineage_registration;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.collection.RefList;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.sort_tree.FlipDescendants;

public class LineageRegistrationUtils
{

	public static void sortTrackSchemeToMatchReferenceFirst( Model embryoA, Model embryoB )
	{
		ModelGraph graphA = embryoA.getGraph();
		ModelGraph graphB = embryoB.getGraph();
		RefRefMap< Spot, Spot > roots = RootsPairing.pairDividingRoots( graphA, graphB );
		AffineTransform3D transformAB = EstimateTransformation.estimateScaleRotationAndTranslation( roots );
		LineageRegistrationAlgorithm algorithm = new LineageRegistrationAlgorithm(
				graphA, graphB,
				roots, transformAB );
		RefList< Spot > flip = algorithm.getToBeFlipped();
		FlipDescendants.flipDescendants( embryoB, flip );
	}
}
