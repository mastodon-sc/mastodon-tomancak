package org.mastodon.mamut.tomancak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.ColorUtils;
import org.mastodon.util.TagSetUtils;
import org.scijava.Cancelable;
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
public class CellDivisionsTagSetCommand implements Command, Cancelable
{
	@Parameter
	private MamutAppModel appModel;

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
		appModel.getBranchGraphSync().sync();
		model = appModel.getModel();
		createTagSet();
		applyTags();
	}

	private void createTagSet()
	{
		int defaultColor = this.defaultColor.getARGB();
		int highlightColor = this.highlightColor.getARGB();
		List< Pair< String, Integer > > tagColors = new ArrayList<>( Collections.nCopies( 2 * n + 1, null ) );
		tagColors.set( 0, Pair.of( "default", defaultColor ) );
		for ( int i = 0; i < n; i++ )
		{
			int color = ColorUtils.mixColors( highlightColor, defaultColor, ( float ) i / n );
			String label = "T-" + ( i + 1 );
			tagColors.set( n - i, Pair.of( label, color ) );
		}
		for ( int i = 0; i < n; i++ )
		{
			int color = ColorUtils.mixColors( highlightColor, defaultColor, ( float ) i / n );
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

	private TagSetStructure.Tag getTag( boolean dividesBefore,
			boolean dividesAfter, int startOffset, int endOffset )
	{
		if ( dividesAfter && ( endOffset < tagsBefore.size() ) )
			return tagsBefore.get( endOffset );
		if ( dividesBefore && ( startOffset < tagsAfter.size() ) )
			return tagsAfter.get( startOffset );
		return backgroundTag;
	}

	@Override
	public boolean isCanceled()
	{
		return false;
	}

	@Override
	public void cancel( String reason )
	{

	}

	@Override
	public String getCancelReason()
	{
		return null;
	}
}
