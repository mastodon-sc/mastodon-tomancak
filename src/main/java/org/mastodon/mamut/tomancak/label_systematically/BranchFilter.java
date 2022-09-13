package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.function.Predicate;

class BranchFilter implements Predicate<Spot>
{

	private final ModelGraph graph;

	private boolean matchUnnamed = false;

	private Predicate<String> labelFilter = null;

	public BranchFilter( final ModelGraph graph )
	{
		this.graph = graph;
	}

	public void setMatchUnnamed( boolean matchUnnamed )
	{
		this.matchUnnamed = matchUnnamed;
	}

	public void setLabelFilter( Predicate<String> namePattern )
	{
		this.labelFilter = namePattern;
	}

	void setLabelEndsWith1or2Filter()
	{
		setLabelFilter( label -> label.endsWith( "1" ) || label.endsWith( "2" ) );
	}

	@Override
	public boolean test( Spot branchStart )
	{
		if ( labelFilter != null )
		{
			String label = BranchUtils.getBranchLabel( graph, branchStart );
			if ( label != null )
				return labelFilter.test( label );
		}
		return matchUnnamed && !BranchUtils.isBranchLabelSet( graph, branchStart );
	}
}
