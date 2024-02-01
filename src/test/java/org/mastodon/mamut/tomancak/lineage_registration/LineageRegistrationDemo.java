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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import mpicbg.spim.data.SpimDataException;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.views.trackscheme.MamutViewTrackScheme;
import org.mastodon.views.trackscheme.display.TrackSchemeFrame;
import org.scijava.Context;

public class LineageRegistrationDemo
{
	public static final String project1 = "/home/arzt/Datasets/Mette/E1.mastodon";
	public static final String project2 = "/home/arzt/Datasets/Mette/E2.mastodon";

	public static void main( String... args )
	{
		Context context = new Context();
		openAppModel( context, project1 );
		openAppModel( context, project2 );
		context.service( LineageRegistrationControlService.class ).showDialog();
	}

	private static void openAppModel( Context context, String projectPath )
	{
		try
		{
			ProjectModel projectModel = ProjectLoader.open( projectPath, context );
			projectModel.getWindowManager().createView( MamutViewTrackScheme.class );
			new MainWindow( projectModel ).setVisible( true );
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

}
