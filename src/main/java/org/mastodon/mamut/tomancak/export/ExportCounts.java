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
package org.mastodon.mamut.tomancak.export;

import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.tomancak.util.SpotsIterator;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.log.Logger;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

@Plugin( type = Command.class, name = "Export spots counts per lineage per time point" )
public class ExportCounts implements Command
{
	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String selectionInfoMsg = "...also of only selected sub-trees.";

	@Parameter(label = "Original time point values:",
			description = "If unchecked, time becomes relative to respective lineage root and will thus start always from zero.")
	boolean reportAbsoluteTime = false;

	@Parameter(label = "Delimiting character:",
			description = "Separator of columns (between time and count) in the exported files.")
	String delim = ",";

	@Parameter(label = "Filenames prefix:",
			description = "Prefix used for every name of the exported files. Leave empty to use no prefix.")
	String fileNamesPrefix = "";

	@Parameter(label = "Output directory:", style = FileWidget.DIRECTORY_STYLE)
	File outputDirectory;

	@Parameter(persist = false)
	private MamutAppModel appModel;

	@Parameter
	private LogService logService;

	@Override
	public void run()
	{
		final Logger loggerRoots = logService.subLogger("Counts exporter...");
		ss = new SpotsIterator(appModel, loggerRoots);

		if (appModel.getSelectionModel().isEmpty())
			ss.visitRootsFromEntireGraph(this::processOneLineage);
		else
			ss.visitRootsFromSelection(this::processOneLineage);
	}

	SpotsIterator ss;
	final Map<Integer,Integer> tpToCnts = new HashMap<>(2000);

	void processOneLineage(final Spot rootSpot)
	{
		tpToCnts.clear();
		final int refTP = reportAbsoluteTime ? 0 : rootSpot.getTimepoint();
		ss.visitDownstreamSpots( rootSpot,
				s -> tpToCnts.put(s.getTimepoint()-refTP, 1+tpToCnts.getOrDefault(s.getTimepoint()-refTP,0)) );
		writeCSV(rootSpot.getLabel());
	}

	void writeCSV(final String treeLabel)
	{
		final String filename = outputDirectory.getAbsolutePath()+File.separator+fileNamesPrefix+treeLabel+".csv";
		logService.info("Writing file: "+filename);
		try ( PrintWriter file = new PrintWriter(new FileWriter(filename)) )
		{
			for (Map.Entry<Integer,Integer> tp : tpToCnts.entrySet())
				file.println(tp.getKey()+delim+tp.getValue());
		} catch (IOException e) {
			logService.error("Error writing a file "+filename+":"+e.getMessage());
		}
	}
}
