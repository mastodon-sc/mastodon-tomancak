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
package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.function.Predicate;

import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class LineageTreeUtils
{
	/**
	 * @return the set of root nodes of the given graph.
	 */
	public static RefSet< Spot > getRoots( ModelGraph graph )
	{
		Predicate< Spot > isRoot = spot -> spot.incomingEdges().isEmpty();
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	public static RefSet< Spot > getRoots( ModelGraph graph, int timepoint )
	{
		Predicate< Spot > isRoot = spot -> spot.getTimepoint() == timepoint
				|| ( spot.incomingEdges().isEmpty() && spot.getTimepoint() > timepoint );
		return RefCollectionUtils.filterSet( graph.vertices(), isRoot );
	}

	/**
	 * @return true if the given spot is part of a branch that divides.
	 */
	public static boolean doesBranchDivide( final Spot spot, final Spot ref )
	{
		Spot branchEnd = BranchGraphUtils.getBranchEnd( spot, ref );
		return branchEnd.outgoingEdges().size() > 1;
	}
}
