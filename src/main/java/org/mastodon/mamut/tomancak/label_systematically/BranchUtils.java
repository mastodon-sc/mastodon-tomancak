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
