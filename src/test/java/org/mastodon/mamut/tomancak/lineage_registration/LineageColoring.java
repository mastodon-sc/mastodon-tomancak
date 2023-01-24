package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefRefMap;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class LineageColoring
{
	/**
	 * Assigns a tag to the lineages in two models, model A and modelB:
	 * <p>
	 * Creates a new tag set "lineages" in both models.
	 * A tag is added for all the root nodes that where successfully matched using {@link RootsPairing#pairRoots}.
	 * The tags of matching roots node gets the same colors assigned.
	 * Finally the tag is applied to all the descendants of a root node.
	 */	
	public static void tagLineages(Model embryoA, Model embryoB)
	{
		RefRefMap< Spot, Spot > roots = RootsPairing.pairRoots( embryoA.getGraph(), embryoB.getGraph() );
		Set< String > labels = roots.keySet().stream().map( Spot::getLabel ).collect( Collectors.toSet() );
		Map<String, Integer> colorMap = createColorMap( labels );
		tagLineages( colorMap, roots.keySet(), embryoA );
		tagLineages( colorMap, roots.values(), embryoB );
	}

	private static void tagLineages( Map<String, Integer> colorMap, RefCollection< Spot > roots, Model model )
	{
		TagSetStructure.TagSet tagSet = createTagSet( model, "lineages", colorMap );
		Map< String, TagSetStructure.Tag > tags = tagSet.getTags().stream()
				.collect( Collectors.toMap( TagSetStructure.Tag::label, tag -> tag ) );
		for( Spot root : roots ) {
			tagLineage( model, root, tagSet, tags.get(root.getLabel()) );
		}
	}

	private static void tagLineage( Model model, Spot root, TagSetStructure.TagSet tagSet, TagSetStructure.Tag tag )
	{
		ObjTagMap<Spot, TagSetStructure.Tag> spotTags = model.getTagSetModel().getVertexTags().tags( tagSet );
		ObjTagMap< Link, TagSetStructure.Tag> edgeTags = model.getTagSetModel().getEdgeTags().tags( tagSet );

		SearchListener<Spot, Link, DepthFirstSearch<Spot, Link> > searchListener = new SearchListener<Spot, Link, DepthFirstSearch<Spot, Link>>()
		{
			@Override
			public void processVertexLate( Spot spot, DepthFirstSearch<Spot, Link> search )
			{
				// do nothing
			}

			@Override
			public void processVertexEarly( Spot spot, DepthFirstSearch<Spot, Link> spotLinkDepthFirstSearch )
			{
				spotTags.set( spot, tag );
			}

			@Override
			public void processEdge( Link link, Spot spot, Spot v1, DepthFirstSearch<Spot, Link> spotLinkDepthFirstSearch )
			{
				edgeTags.set( link, tag );
			}

			@Override
			public void crossComponent( Spot spot, Spot v1, DepthFirstSearch<Spot, Link> spotLinkDepthFirstSearch )
			{
				// do nothing
			}
		};

		DepthFirstSearch<Spot, Link> search = new DepthFirstSearch<>( model.getGraph(), GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( searchListener );
		search.start( root );
	}

	public static TagSetStructure.TagSet createTagSet( Model model, String title, Map<String, Integer> tagsAndColors )
	{
		TagSetModel<Spot, Link> tagSetModel = model.getTagSetModel();
		TagSetStructure tss = copy( tagSetModel.getTagSetStructure() );
		TagSetStructure.TagSet tagSet = tss.createTagSet(title);
		tagsAndColors.forEach( tagSet::createTag );
		tagSetModel.setTagSetStructure( tss );
		return tagSet;
	}

	private static TagSetStructure copy( TagSetStructure original )
	{
		TagSetStructure copy = new TagSetStructure();
		copy.set( original );
		return copy;
	}

	@NotNull
	private static Map<String, Integer> createColorMap( Set<String> labels )
	{
		Map<String, Integer> colors = new HashMap<>();
		int count = 4;
		for(String label : labels )
			colors.put( label, Glasbey.GLASBEY[ count++ ] );
		return colors;
	}
}
