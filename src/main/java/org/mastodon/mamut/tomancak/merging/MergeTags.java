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
package org.mastodon.mamut.tomancak.merging;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public class MergeTags
{
	public static class TagSetStructureMaps
	{
		final Map< TagSet, TagSet > tagSetMap = new HashMap<>();
		final Map< Tag, Tag > tagMap = new HashMap<>();
	}

	/**
	 * Adds all TagSets from {@code source} to {@code target}, prefixed with
	 * {@code prefix}.
	 * 
	 * @param target
	 *            the target {@link TagSetStructure}.
	 * @param source
	 *            the source {@link TagSetStructure}.
	 * @param prefix
	 *            the prefix to add.
	 * @return a new {@link TagSetStructureMaps} with the merged TagSets.
	 */
	public static TagSetStructureMaps addTagSetStructureCopy( final TagSetStructure target, final TagSetStructure source, final String prefix )
	{
		final TagSetStructureMaps maps = new TagSetStructureMaps();
		for ( final TagSet tagSet : source.getTagSets() )
		{
			final TagSet newTagSet = target.createTagSet( prefix + tagSet.getName() );
			maps.tagSetMap.put( tagSet, newTagSet );
			for ( final Tag tag : tagSet.getTags() )
			{
				final Tag newTag = newTagSet.createTag( tag.label(),  tag.color() );
				maps.tagMap.put( tag, newTag );
			}
		}
		return maps;
	}

	/**
	 * Merge all TagSets from {@code source} into {@code merged}.
	 * 
	 * @param merged
	 *            the target {@link TagSetStructure}.
	 * @param source
	 *            the source {@link TagSetStructure}.
	 * @return a new {@link TagSetStructureMaps} with the merged TagSets.
	 */
	public static TagSetStructureMaps mergeTagSetStructure( final TagSetStructure merged, final TagSetStructure source )
	{
		final Map< String, TagSet > nameToMergedTagSet = new HashMap<>();
		merged.getTagSets().forEach( tagSet -> nameToMergedTagSet.put( tagSet.getName(), tagSet ) );

		final TagSetStructureMaps maps = new TagSetStructureMaps();
		for ( final TagSet tagSet : source.getTagSets() )
		{
			final String tsn = tagSet.getName();
			final TagSet mergedTagSet = nameToMergedTagSet.computeIfAbsent( tsn, merged::createTagSet );
			maps.tagSetMap.put( tagSet, mergedTagSet );
			for ( final Tag tag : tagSet.getTags() )
			{
				final String tn = tag.label();
				final int tc = tag.color();
				Tag mergedTag = getTag( mergedTagSet, tn );
				if ( mergedTag == null )
					mergedTag = mergedTagSet.createTag( tn, tc );
				maps.tagMap.put( tag, mergedTag );
			}
		}

		return maps;
	}

	private static Tag getTag( final TagSet tagSet, final String tagName )
	{
		return tagSet.getTags().stream()
				.filter( tag -> tag.label().equals( tagName ) )
				.findFirst()
				.orElse( null );
	}
}
