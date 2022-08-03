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
/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2021, 2022, Vladimír Ulman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mastodon.mamut.tomancak.spots;

import org.apache.commons.lang.StringUtils;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.tomancak.util.SpotsIterator;
import org.mastodon.mamut.MamutAppModel;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.log.Logger;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, name = "Filter lineage and remove solists spots" )
public class FilterOutSolists implements Command
{
	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false, required = false)
	final String hintMsg = "A spot-solist has no ancestors and no descendants, plus the optional conditions below:";

	@Parameter(label = "And the spot must appear in the last time point:",
			description = "Lonely spots at the end of the video are much harder" +
					"to find compared to lonely spots at the beginning.")
	private boolean isInTheLastTimePoint = true;

	@Parameter(label = "And the spot's label consists of numbers only:",
			description = "A label that is not a number only suggests that" +
					"user has curated this spot.")
	private boolean hasLabelMadeOfNumbersOnly = true;

	@Parameter(persist = false)
	private MamutAppModel appModel;

	@Parameter
	private LogService logService;

	@Override
	public void run()
	{
		final Logger loggerRoots = logService.subLogger("Searcher...");
		final Logger loggerSolists = logService.subLogger("Remove Solists");

		final RefList<Spot> solists = new RefArrayList<>(appModel.getModel().getGraph().vertices().getRefPool(),100);
		final int maxTP = appModel.getMaxTimepoint();

		final SpotsIterator ss = new SpotsIterator(appModel, loggerRoots);
		ss.visitAllSpots(s -> {
			if (ss.countAncestors(s) == 0 && ss.countDescendants(s) == 0) {
				loggerSolists.info("Potential solist: "+s.getLabel()+" at timepoint "+s.getTimepoint());
				//
				if (isInTheLastTimePoint && s.getTimepoint() != maxTP) return;
				if (hasLabelMadeOfNumbersOnly && !StringUtils.isNumeric(s.getLabel())) return;
				loggerSolists.info("                  ^^^^^ will be removed");
				solists.add(s);
			}});

		final ModelGraph g = appModel.getModel().getGraph();
		for (Spot s : solists) g.remove(s);
		appModel.getModel().getGraph().notifyGraphChanged();
	}
}
