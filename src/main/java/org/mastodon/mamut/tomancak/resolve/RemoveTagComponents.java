/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
			run( model, tagSet, spots );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
		graph.notifyGraphChanged();
	}

	static void run( final Model model, final TagSetStructure.TagSet tagSet, final Collection< Spot > spots )
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
		// Perform a depth-first search visiting all the spots that have the same "tag"
		// as the given "spot". The tag is removed from all the visited spots. Removing
		// the tag at the same time helps to depth-first search to prevent visiting the
		// same spot multiple times.
		final Spot ref1 = graph.vertexRef();
		final Spot ref2 = graph.vertexRef();
		try
		{
			final RefStack< Spot > spotStack = RefCollections.createRefStack( graph.vertices() );
			spotStack.push( spot );
			while ( !spotStack.isEmpty() )
			{
				final Spot s = spotStack.pop( ref1 );
				if ( tag.equals( vertexTags.get( s ) ) )
				{
					vertexTags.remove( s );
					for ( final Link link : s.incomingEdges() )
					{
						if ( tag.equals( linkTags.get( link ) ) )
							linkTags.remove( link );
						spotStack.push( link.getSource( ref2 ) );
					}
					for ( final Link link : s.outgoingEdges() )
					{
						if ( tag.equals( linkTags.get( link ) ) )
							linkTags.remove( link );
						spotStack.push( link.getTarget( ref2 ) );
					}
				}
			}
		}
		finally
		{
			graph.releaseRef( ref1 );
			graph.releaseRef( ref2 );
		}
	}
}
