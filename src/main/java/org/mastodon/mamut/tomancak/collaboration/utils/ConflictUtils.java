package org.mastodon.mamut.tomancak.collaboration.utils;

import java.util.List;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

public class ConflictUtils
{
	private ConflictUtils()
	{
		// prevent from instantiation
	}

	public static boolean hasConflict( Model model )
	{
		TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		return !isTagSetEmpty( tagSetModel, "Merge Conflict", "Conflict" ) ||
				!isTagSetEmpty( tagSetModel, "Merge Conflict (Tags)", "Tag Conflict" ) ||
				!isTagSetEmpty( tagSetModel, "Merge Conflict (Labels)", "Label Conflict" );
	}

	public static void removeMergeConflictTagSets( Model model )
	{
		TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		TagSetStructure original = tagSetModel.getTagSetStructure();
		TagSetStructure replacement = new TagSetStructure();
		replacement.set( original );
		for ( TagSetStructure.TagSet tagSet : original.getTagSets() )
			if ( isConflictTagSetName( tagSet.getName() ) )
				replacement.remove( tagSet );
		tagSetModel.setTagSetStructure( replacement );
	}

	private static boolean isConflictTagSetName( String name )
	{
		return name.equals( "Merge Conflict" ) ||
				name.equals( "Merge Conflict (Tags)" ) ||
				name.equals( "Merge Conflict (Labels)" ) ||
				name.equals( "Merge Source A" ) ||
				name.equals( "Merge Source B" ) ||
				name.startsWith( "((A)) " ) ||
				name.startsWith( "((B)) " );
	}

	/**
	 * Returns true if the given tag set is empty or if it does not exist.
	 */
	private static boolean isTagSetEmpty( TagSetModel< Spot, Link > tagSetModel, String tagSetName, String tagLabel )
	{
		TagSetStructure tagSetStructure = tagSetModel.getTagSetStructure();
		List< TagSetStructure.TagSet > tagSets = tagSetStructure.getTagSets();
		TagSetStructure.TagSet tagSet = tagSets.stream().filter( ts -> tagSetName.equals( ts.getName() ) ).findFirst().orElse( null );
		if ( tagSet == null )
			return true;
		TagSetStructure.Tag tag = tagSet.getTags().stream().filter( t -> tagLabel.equals( t.label() ) ).findFirst().orElse( null );
		if ( tag == null )
			return true;
		return tagSetModel.getVertexTags().getTaggedWith( tag ).isEmpty() && tagSetModel.getEdgeTags().getTaggedWith( tag ).isEmpty();
	}
}
