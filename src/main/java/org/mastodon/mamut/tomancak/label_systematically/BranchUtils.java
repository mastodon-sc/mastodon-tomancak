package org.mastodon.mamut.tomancak.label_systematically;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.regex.Pattern;

class BranchUtils
{
	private static final Pattern onlyDigitsPattern = Pattern.compile( "[0-9]+" );

	/**
	 * Return the label of a branch, if the branch has a consistent label.
	 * The label of a branch is consistent, is all the spots have the same
	 * label. With one exception,  a spot is allowed to not have been renamed.
	 * (Not renamed her means, that the spot label is a number.
	 *
	 * @return label of the branch. Null if there is no consistent label.
	 */
	public static String getBranchLabel( ModelGraph graph, Spot branchStart ) {
		Spot ref = graph.vertexRef();
		try {
			Spot s = branchStart;
			String name = null;
			if( isSpotLabelSet( s ) )
				name = s.getLabel();
			while(s.outgoingEdges().size() == 1) {
				s = s.outgoingEdges().iterator().next().getTarget(ref);
				if( isSpotLabelSet( s ) ) {
					if(name == null)
						name = s.getLabel();
					else if(!name.equals( s.getLabel() ))
						return null;
				}
			}
			return name;
		}
		finally {
			graph.releaseRef( ref );
		}
	}

	/**
	 * @return true, if at least one spot in the branch has a label that is not
	 * a positive integer number.
	 */
	public static boolean isBranchLabelSet( ModelGraph graph, Spot branchStart ) {
		Spot ref = graph.vertexRef();
		try {
			Spot s = branchStart;
			if( isSpotLabelSet( s ) )
				return true;
			while(s.outgoingEdges().size() == 1) {
				s = s.outgoingEdges().iterator().next().getTarget(ref);
				if( isSpotLabelSet( s ) )
					return true;
			}
			return false;
		}
		finally {
			graph.releaseRef( ref );
		}
	}

	/**
	 * @return false if the spot label is a positive integer number.
	 */
	private static boolean isSpotLabelSet( Spot spot ) {
		return ! onlyDigitsPattern.matcher( spot.getLabel() ).matches();
	}
}
