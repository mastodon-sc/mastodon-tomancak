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
package org.mastodon.mamut.tomancak;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDummyTagSet
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final Random random = new Random();

	private CreateDummyTagSet()
	{
		// Prevent instantiation.
	}

	static void createDummyTagSet( final ProjectModel projectModel, final CommandService commandService )
	{
		commandService.run( DummyTagSetCommand.class, true, "model", projectModel.getModel() );
	}

	@Plugin( type = Command.class, label = "Create dummy tag set" )
	public static class DummyTagSetCommand implements Command
	{

		private static final int WIDTH = 15;

		@SuppressWarnings( "all" )
		@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
		private String documentation = "<html>\n"
				+ "<body width=" + WIDTH + "cm align=left>\n"
				+ "<h2>Create a dummy tag set with a specifiable number of tags</h2>\n"
				+ "<p>"
				+ "This plugin generates a new tag set with a specifiable number of tags in random colors.<br><br>"
				+ "Mastodon can handle tousands of tags, but the maximum number is limited by the system of the user.<br><br>"
				+ "An error message while trying to create the tag set might indicate that the specified number of tags exceeds the maximum number the Mastodon installation can handle.<br>"
				+ "</p>\n"
				+ "</body>\n"
				+ "</html>\n";

		@Parameter
		private Model model;

		@Parameter
		private Context context;

		@Parameter( label = "Tag set name", description = "Specify the name of the tag set that should be generated." )
		private String tagSetName;

		@Parameter( label = "Maximum number of tags", description = "Specify the number tags that should be generated in a dummy tag set.", min = "0", stepSize = "1" )
		private int maxTags;

		@Override
		public void run()
		{
			createTagSet( model, tagSetName, maxTags );
		}

		private static void createTagSet( final Model model, final String tagSetName, final int numTags )
		{
			logger.info( "Creating tag set '{}' with {} dummy tags...", tagSetName, numTags );
			final ReentrantReadWriteLock lock = model.getGraph().getLock();
			lock.writeLock().lock();
			try
			{
				Collection< Pair< String, Integer > > labelColorPairs = new ArrayList<>();
				for ( int i = 0; i < numTags; i++ )
					labelColorPairs.add( Pair.of( "tag" + i, getRandomColor().getRGB() ) );

				TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, labelColorPairs );
				model.setUndoPoint();

				logger.info( "Successfully added tag set." );
				logger.info( "Tag set name: {} ", tagSet.getName() );
				logger.info( "Number of tags: {}", tagSet.getTags().size() );
			}
			finally
			{
				lock.writeLock().unlock();
			}
		}

		private static Color getRandomColor()
		{
			// Generate random RGB values
			int red = random.nextInt( 256 );
			int green = random.nextInt( 256 );
			int blue = random.nextInt( 256 );

			// Create the color using the RGB values
			return new Color( red, green, blue );
		}
	}
}
