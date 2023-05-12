package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefRefMap;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;

public class LineageColoring
{
	/**
	 * Assigns a tag to the lineages in two models, model A and modelB:
	 * <p>
	 * Creates a new tag set "lineages" in both models.
	 * A tag is added for all the root nodes that where successfully matched using {@link RootsPairing#pairDividingRoots}.
	 * The tags of matching roots node gets the same colors assigned.
	 * Finally, the tag is applied to all the descendants of a root node.
	 */
	public static void tagLineages( Model embryoA, int firstTimepointA, Model embryoB, int firstTimepointB )
	{
		RefRefMap< Spot, Spot > roots = RootsPairing.pairDividingRoots(
				embryoA.getGraph(), firstTimepointA,
				embryoB.getGraph(), firstTimepointB );
		List< String > labels = roots.keySet().stream().map( Spot::getLabel ).sorted().collect( Collectors.toList() );
		Map< String, Integer > colorMap = createColorMap( labels );
		tagLineages( colorMap, roots.keySet(), embryoA );
		tagLineages( colorMap, roots.values(), embryoB );
	}

	private static void tagLineages( Map< String, Integer > colorMap, RefCollection< Spot > roots, Model model )
	{
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "lineages", colorMap.entrySet() );
		Map< String, TagSetStructure.Tag > tags = TagSetUtils.tagSetAsMap( tagSet );
		for ( Spot root : roots )
			tagLineage( model, root, tagSet, tags.get( root.getLabel() ) );
	}

	private static void tagLineage( Model model, Spot root, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		ObjTagMap< Spot, TagSetStructure.Tag > spotTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		ObjTagMap< Link, TagSetStructure.Tag > edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );
		DepthFirstIterator< Spot, Link > iterator = new DepthFirstIterator<>( root, model.getGraph() );
		while ( iterator.hasNext() )
		{
			Spot spot = iterator.next();
			spotTags.set( spot, tag );
			for ( Link edge : spot.outgoingEdges() )
				edgeTags.set( edge, tag );
		}
	}

	/**
	 * For a given set of labels, create a map from label to color.
	 * Colors are taken from the Glasbey lookup table.
	 */
	private static Map< String, Integer > createColorMap( Collection< String > labels )
	{
		Map< String, Integer > colors = new HashMap<>();
		int count = 4;
		for ( String label : labels )
			colors.put( label, Glasbey.GLASBEY[ count++ ] );
		return colors;
	}

}
