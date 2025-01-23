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
package org.mastodon.mamut.tomancak.export;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.scijava.app.StatusService;

/**
 * Tests {@link ExportDivisionCountsPerTimepointCommand}
 */
public class ExportDivisionCountsPerTimepointCommandTest
{

	@Test
	public void testWriteDivisionCountsToFile() throws IOException
	{
		try (Context context = new Context())
		{
			Model model = new ExampleGraph2().getModel();
			File outputFile = File.createTempFile( "divisioncounts", ".csv" );
			outputFile.deleteOnExit();
			StatusService service = context.service( StatusService.class );

			ExportDivisionCountsPerTimepointCommand.writeDivisionCountsToFile( model, outputFile, service );

			String content = FileUtils.readFileToString( outputFile );
			String expected = "\"timepoint\",\"divisions\"\n"
					+ "0,0\n"
					+ "1,0\n"
					+ "2,1\n"
					+ "3,0\n"
					+ "4,1\n"
					+ "5,0\n"
					+ "6,0\n"
					+ "7,0\n";
			assertEquals( expected, content );
		}
	}
}
