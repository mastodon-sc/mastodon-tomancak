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
	 * Adds all TagSets from {@code source} to {@code target}, prefixed with {@code prefix}.
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
