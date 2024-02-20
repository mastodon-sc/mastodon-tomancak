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
package org.mastodon.mamut.tomancak.export;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.util.TreeUtils;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

import sun.tools.java.ClassNotFound;

@Plugin( type = Command.class, label = "Export spot counts per timepoint" )
public class ExportSpotCountsPerTimepointCommand implements Command
{

	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=15cm align=left>\n"
			+ "<h1>Export spot counts per timepoint</h1>\n"
			+ "<p>This command writes the timepoint and the number of spots at each timepoint to a single CSV file.</p>\n"
			+ "<p>The format is: \"timepoint\", \"number of spots\".</p>\n"
			+ "<p>It is recommended to use '*.csv' as file extension.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter( label = "Save to" )
	private File saveTo;

	@Parameter
	private ProjectModel projectModel;

	@Parameter
	private Context context;

	@Override
	public void run()
	{
		try
		{
			writeSpotCountsToFile( projectModel.getModel(), saveTo, context.service( StatusService.class ) );
		}
		catch ( IOException e )
		{
			System.err.println( "Could not write spot counts to file: " + saveTo.getAbsolutePath() + ". Error message: " + e.getMessage() );
		}
	}

	/**
	 * Writes all timepoints and the number of spots for each timepoint to the given file.
	 * <ul>
	 *     <li>The file will be overwritten if it already exists.</li>
	 *     <li>The file will be created if it does not exist.</li>
	 *     <li>The format is: "timepoint", "number of spots".</li>
	 *     <li>The first line is the header.</li>
	 * </ul>
	 */
	public static void writeSpotCountsToFile( final Model model, final File file, final StatusService statusService ) throws IOException
	{
		if ( file == null )
			throw new IllegalArgumentException( "Cannot write spot counts to file. Given file is null." );

		try (CSVWriter csvWriter = new CSVWriter( new FileWriter( file ) ))
		{
			csvWriter.writeNext( new String[] { "timepoint", "spots" } );
			int minTimepoint = TreeUtils.getMinTimepoint( model );
			int maxTimepoint = TreeUtils.getMaxTimepoint( model );
			for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			{
				int spots = model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size();
				csvWriter.writeNext( new String[] { String.valueOf( timepoint ), String.valueOf( spots ) }, false );
				statusService.showProgress( timepoint, maxTimepoint );
			}
		}
	}
}
