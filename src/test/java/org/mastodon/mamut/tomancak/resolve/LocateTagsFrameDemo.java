package org.mastodon.mamut.tomancak.resolve;

import java.io.IOException;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class LocateTagsFrameDemo
{
	public static void main( final String... args ) throws SpimDataException, IOException
	{
		final Context context = new Context();
		final ProjectModel projectModel = ProjectLoader.open( "/home/arzt/Datasets/Mette/E1.mastodon", context );
		final LocateTagsFrame frame = new LocateTagsFrame( projectModel );
		frame.pack();
		frame.setVisible( true );
	}
}
