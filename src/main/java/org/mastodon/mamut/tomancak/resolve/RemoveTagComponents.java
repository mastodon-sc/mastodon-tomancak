package org.mastodon.mamut.tomancak.resolve;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefStack;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

/**
 * Class for removing a "connected component of tags" from a model.
 */
public class RemoveTagComponents
{
	/**
	 * For the given tag set remove the tag from the given spots and all the
	 * spots that are connected to them by edges and have the same tag. The
	 * tag for connected edges is also removed.
	 */
	public static void run( final ProjectModel projectModel, final TagSetStructure.TagSet tagSet, final Collection< Spot > spots )
	{
		final Model model = projectModel.getModel();
		final ModelGraph graph = projectModel.getModel().getGraph();
		final ReentrantReadWriteLock.WriteLock lock = graph.getLock().writeLock();
		lock.lock();
		try
		{
			run( model, spots, tagSet );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
		graph.notifyGraphChanged();
	}

	private static void run( final Model model, final Collection< Spot > spots, final TagSetStructure.TagSet tagSet )
	{
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final ObjTagMap< Spot, TagSetStructure.Tag > spotTags = tagSetModel.getVertexTags().tags( tagSet );
		final ObjTagMap< Link, TagSetStructure.Tag > linkTags = tagSetModel.getEdgeTags().tags( tagSet );
		for ( final Spot spot : spots )
			removeTagConnectedComponent( model.getGraph(), spot, spotTags, linkTags );
	}

	private static void removeTagConnectedComponent( final ModelGraph graph, final Spot spot, final ObjTagMap< Spot, TagSetStructure.Tag > vertexTags,
			final ObjTagMap< Link, TagSetStructure.Tag > linkTags )
	{
		final TagSetStructure.Tag tag = vertexTags.get( spot );
		if ( tag == null )
			return;
		final RefStack< Spot > spotStack = RefCollections.createRefStack( graph.vertices() );
		spotStack.push( spot );
		while ( !spotStack.isEmpty() )
		{
			final Spot s = spotStack.pop();
			if ( tag.equals( vertexTags.get( s ) ) )
			{
				vertexTags.remove( s );
				for ( final Link link : s.incomingEdges() )
				{
					if ( tag.equals( linkTags.get( link ) ) )
						linkTags.remove( link );
					spotStack.push( link.getSource( graph.vertexRef() ) );
				}
				for ( final Link link : s.outgoingEdges() )
				{
					if ( tag.equals( linkTags.get( link ) ) )
						linkTags.remove( link );
					spotStack.push( link.getTarget( graph.vertexRef() ) );
				}
			}
		}
	}
}