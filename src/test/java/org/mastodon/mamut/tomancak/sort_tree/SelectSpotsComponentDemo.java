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
package org.mastodon.mamut.tomancak.sort_tree;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import mpicbg.spim.data.SpimDataException;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import java.io.IOException;

public class SelectSpotsComponentDemo
{
	public static void main(String... args)
			throws IOException, SpimDataException
	{
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( new MamutProjectIO().load( "/home/arzt/Datasets/Mette/E1.mastodon" ) );
		MamutAppModel appModel = windowManager.getAppModel();
		JFrame frame = new JFrame("SelectSpotsComponent Demo");
		frame.setLayout( new MigLayout() );
		frame.add(new JLabel("Select:"), "wrap");
		frame.add(new SelectSpotsComponent( appModel ));
		frame.pack();
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
}
