/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.tomancak.collaboration.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewDirectoryUtils
{

	/**
	 * If {@code newSubdirectory} is true, create a new subdirectory in the
	 * given {@code directory}. The name of the subdirectory is extracted from
	 * the {@code repositoryURL}.
	 * <p>
	 * If {@code newSubdirectory} is false, return the {@code directory} as it is.
	 */
	static File createRepositoryDirectory( boolean newSubdirectory, File directory, String repositoryURL ) throws IOException
	{
		if ( !newSubdirectory )
			return directory;

		return createSubdirectory( directory, extractRepositoryName( repositoryURL ) );
	}

	static File createSubdirectory( File parentDirectory, String repositoryName ) throws IOException
	{
		File directory = new File( parentDirectory, repositoryName );
		if ( directory.isDirectory() )
		{
			if ( isEmptyDirectory( directory ) )
				return directory;
			else
				throw new IOException( "Directory already exists but is not empty: " + directory
						+ "\nPlease move or delete the directory and try again." );
		}
		Files.createDirectory( directory.toPath() );
		return directory;
	}

	private static boolean isEmptyDirectory( File directory )
	{
		String[] list = directory.list();
		if ( list == null ) // not a directory
			return false;
		return list.length == 0;
	}

	static String extractRepositoryName( String repositoryURL )
	{
		Pattern pattern = Pattern.compile( "/([\\w-]+)(\\.git|/)?$" );
		Matcher matcher = pattern.matcher( repositoryURL );
		if ( matcher.find() )
			return matcher.group( 1 );
		throw new IllegalArgumentException( "Could not extract repository name from URL:" + repositoryURL );
	}
}
