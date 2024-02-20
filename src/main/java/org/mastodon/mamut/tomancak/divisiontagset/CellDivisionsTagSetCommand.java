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
package org.mastodon.mamut.tomancak.divisiontagset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.tomancak.util.DefaultCancelable;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.ColorUtils;
import org.mastodon.util.TagSetUtils;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * {@link Command A SciJava command} that is called by the {@link CellDivisionsTagSetPlugin}.
 * <p>
 * The command creates a tag set that highlights cell divisions. The user interface allows to
 * specify the number of spots to highlight before and after a division, and the highlight and
 * background colors.
 */
@Plugin( type = Command.class, label = "Add a Tag Set to Highlight Cell Divisions", visible = false )
public class CellDivisionsTagSetCommand extends DefaultCancelable implements Command
{
	@Parameter
	private ProjectModel projectModel;

	@Parameter( label = "Tag set name" )
	private String tagSetName = "cell divisions";

	@Parameter( label = "Default color" )
	private ColorRGB defaultColor = new ColorRGB( "dark gray" );

	@Parameter( label = "Highlight color" )
	private ColorRGB highlightColor = new ColorRGB( "pink" );

	@Parameter( label = "Timepoints to highlight before and after cell division" )
	private int n = 3;

	private List< TagSetStructure.Tag > tagsBefore;

	private List< TagSetStructure.Tag > tagsAfter;

	private TagSetStructure.Tag backgroundTag;

	private Model model;

	@Override
	public void run()
	{
		projectModel.getBranchGraphSync().sync();
		model = projectModel.getModel();
		createTagSet();
		applyTags();
	}

	private void createTagSet()
	{
		int defaultARGB = this.defaultColor.getARGB();
		int highlightARGB = this.highlightColor.getARGB();
		List< Pair< String, Integer > > tagColors = new ArrayList<>( Collections.nCopies( 2 * n + 1, null ) );
		tagColors.set( 0, Pair.of( "default", defaultARGB ) );
		for ( int i = 0; i < n; i++ )
		{
			int color = ColorUtils.mixColors( highlightARGB, defaultARGB, ( float ) i / n );
			String label = "T-" + ( i + 1 );
			tagColors.set( n - i, Pair.of( label, color ) );
		}
		for ( int i = 0; i < n; i++ )
		{
			int color = ColorUtils.mixColors( highlightARGB, defaultARGB, ( float ) i / n );
			String label = "T" + ( i + 1 );
			tagColors.set( n + i + 1, Pair.of( label, color ) );
		}
		TagSetStructure.TagSet tagset = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagColors );
		List< TagSetStructure.Tag > allTags = tagset.getTags();
		backgroundTag = allTags.get( 0 );
		tagsBefore = IntStream.range( 0, n ).mapToObj( i -> allTags.get( n - i ) ).collect( Collectors.toList() );
		tagsAfter = IntStream.range( 0, n ).mapToObj( i -> allTags.get( n + i + 1 ) ).collect( Collectors.toList() );
	}

	private void applyTags()
	{
		ModelBranchGraph branchGraph = model.getBranchGraph();
		for ( BranchSpot branch : branchGraph.vertices() )
		{
			int start = branch.getFirstTimePoint();
			int end = branch.getTimepoint();
			boolean dividesBefore = !branch.incomingEdges().isEmpty();
			boolean dividesAfter = !branch.outgoingEdges().isEmpty();
			Iterator< Spot > iterator = branchGraph.vertexBranchIterator( branch );
			while ( iterator.hasNext() )
			{
				Spot spot = iterator.next();
				int time = spot.getTimepoint();
				int startOffset = time - start;
				int endOffset = end - time;
				TagSetStructure.Tag tag = getTag( dividesBefore, dividesAfter, startOffset, endOffset );
				model.getTagSetModel().getVertexTags().set( spot, tag );
			}
			branchGraph.releaseIterator( iterator );
		}
	}

	private TagSetStructure.Tag getTag( boolean dividesBefore, boolean dividesAfter, int startOffset, int endOffset )
	{
		if ( dividesAfter && ( endOffset < tagsBefore.size() ) )
			return tagsBefore.get( endOffset );
		if ( dividesBefore && ( startOffset < tagsAfter.size() ) )
			return tagsAfter.get( startOffset );
		return backgroundTag;
	}
}
