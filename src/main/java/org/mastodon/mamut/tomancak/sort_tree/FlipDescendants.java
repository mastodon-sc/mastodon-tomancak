/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.sort_tree;

import org.mastodon.graph.ref.OutgoingEdges;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FlipDescendants
{
	public static void flipDescendants( final MamutAppModel appModel )
	{
		final Spot spot = appModel.getFocusModel().getFocusedVertex( appModel.getModel().getGraph().vertexRef() );
		if(spot == null)
			return;
		final Model model = appModel.getModel();
		flipDescendants( model, Collections.singleton( spot ) );
	}

	public static void flipDescendants( Model model, Collection<Spot> spots )
	{
		final ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock lock = graph.getLock();
		lock.writeLock().lock();
		try
		{
			for(final Spot spot : spots )
				flipChildren( graph, model.getTagSetModel(), spot );
			model.setUndoPoint();
		}
		finally
		{
			lock.writeLock().unlock();
		}
		graph.notifyGraphChanged();
	}

	private static void flipChildren( ModelGraph graph, TagSetModel<Spot, Link> tagSetModel, Spot spot )
	{
		final OutgoingEdges<Link> outgoing = spot.outgoingEdges();
		if ( outgoing.size() > 1 )
		{
			final Link first = outgoing.get( 0 );
			final Spot target = first.getTarget();
			Map<TagSetStructure.TagSet, TagSetStructure.Tag> tagMap = getTags( tagSetModel, first );
			graph.remove( first );
			final Link newLink = graph.addEdge( spot, target ).init();
			setTags( tagSetModel, tagMap, newLink );
		}
	}

	private static Map< TagSetStructure.TagSet, TagSetStructure.Tag > getTags( TagSetModel< Spot, Link > tagSetModel, Link link )
	{
		Map< TagSetStructure.TagSet, TagSetStructure.Tag > tagMap = new HashMap<>();
		final ObjTags< Link > edgeTagsMap = tagSetModel.getEdgeTags();
		List< TagSetStructure.TagSet > tagSets = tagSetModel.getTagSetStructure().getTagSets();
		for ( TagSetStructure.TagSet tagSet : tagSets )
			tagMap.put( tagSet, edgeTagsMap.tags( tagSet ).get( link ) );
		return tagMap;
	}

	private static void setTags( TagSetModel< Spot, Link > tagSetModel, Map< TagSetStructure.TagSet, TagSetStructure.Tag > tagMap, Link newLink )
	{
		final ObjTags< Link > edgeTagsMap = tagSetModel.getEdgeTags();
		tagMap.forEach( ( tagSet, tag ) -> edgeTagsMap.tags( tagSet ).set( newLink, tag ) );
	}
}
