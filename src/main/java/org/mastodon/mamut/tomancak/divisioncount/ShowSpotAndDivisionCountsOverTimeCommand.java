/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.divisioncount;

import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Triple;
import org.mastodon.mamut.ProjectModel;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, label = "Show spot and division counts over time" )
public class ShowSpotAndDivisionCountsOverTimeCommand implements Command
{

	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=15cm align=left>\n"
			+ "<h1>Show spot and division counts over time</h1>\n"
			+ "<p>This command allows to set a window size and subsequently plot the number of spots and divisions at each timepoint together with a sliding average acknowledging the given window size.</p>\n"
			+ "<p>A division is defined as a spot with more than one outgoing edge.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter( label = "Sliding Average Window Size:", min = "1", stepSize = "1" )
	private int windowSize = 10;

	@Parameter
	private ProjectModel projectModel;

	@Override
	public void run()
	{
		List< Triple< Integer, Integer, Integer > > divisionCounts =
				DivisionCount.getSpotAndDivisionsPerTimepoint( projectModel.getModel() );
		double[] timepoints = divisionCounts.stream().mapToDouble( Triple::getLeft ).toArray();
		double[] spots = divisionCounts.stream().mapToDouble( Triple::getMiddle ).toArray();
		double[] divisions = divisionCounts.stream().mapToDouble( Triple::getRight ).toArray();

		SwingUtilities.invokeLater( () -> new DivisionCountChart( timepoints, spots, divisions, windowSize ).setVisible( true ) );
	}
}
