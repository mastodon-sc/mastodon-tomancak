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

import static org.mastodon.mamut.tomancak.merging.MergingUtil.getMaxNonEmptyTimepoint;
import static org.mastodon.mamut.tomancak.merging.MergingUtil.getNumTimepoints;

import java.io.IOException;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;

/**
 * Reads model from a mastodon project file.
 * Access to the Model.
 * Convenience and debugging methods.
 */
public class Dataset
{
	private final MamutProject project;

	private final int numTimepoints;

	private final Model model;

	private final int maxNonEmptyTimepoint;

	public Dataset( final String path ) throws IOException
	{
		project = new MamutProjectIO().load( path );
		numTimepoints = getNumTimepoints( project );
		model = new Model();
		try (final MamutProject.ProjectReader reader = project.openForReading())
		{
			model.loadRaw( reader );
		}
		maxNonEmptyTimepoint = getMaxNonEmptyTimepoint( model, numTimepoints );

		verify();
	}

	public Model model()
	{
		return model;
	}

	public MamutProject project()
	{
		return project;
	}

	public int maxNonEmptyTimepoint()
	{
		return maxNonEmptyTimepoint;
	}

	/**
	 * Checks that the model graph is a forest. That is: all spots must have at
	 * most 1 parent, and at most 2 children.
	 * 
	 * @throws IllegalStateException
	 *             if the model is not a forest.
	 */
	public void verify() throws IllegalStateException
	{
		for ( final Spot spot : model.getGraph().vertices() )
		{
			if ( spot.incomingEdges().size() > 1 )
				throw new IllegalStateException( spot + " has more than one parent" );

			if ( spot.outgoingEdges().size() > 2 )
				throw new IllegalStateException( spot + " has more than two children" );
		}
	}
}
