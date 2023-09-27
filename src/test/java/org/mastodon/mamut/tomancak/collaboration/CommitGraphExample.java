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
package org.mastodon.mamut.tomancak.collaboration;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.awtui.CommitGraphPane;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revplot.PlotWalk;

// Inspired by https://git.eclipse.org/r/plugins/gitiles/jgit/jgit/+/84022ac9de54ded6f04ca196264a8f2370e9da9a/org.eclipse.jgit.pgm/src/org/eclipse/jgit/pgm/Glog.java
public class CommitGraphExample
{
	public static void main( String... args ) throws IOException, GitAPIException
	{
		Git git = Git.open( new File( "/home/arzt/tmp/2/mgit-test" ) );
		Repository repository = git.getRepository();
		PlotWalk plotWalk = new PlotWalk( repository );
		plotWalk.markStart( plotWalk.parseCommit( repository.resolve( "HEAD" ) ) );
		JFrame frame = new JFrame( "Commit Graph!" );
		CommitGraphPane comp = new CommitGraphPane();
		comp.getCommitList().source( plotWalk );
		comp.getCommitList().fillTo( Integer.MAX_VALUE );
		JLabel label = new JLabel();
		comp.getSelectionModel().addListSelectionListener( e -> {
			int rowIndex = comp.getSelectedRow();
			label.setText( comp.getCommitList().get( rowIndex ).getId().toString() );
		} );
		frame.add( new JScrollPane( comp ) );
		frame.add( label, BorderLayout.PAGE_END );
		frame.setSize( 600, 600 );
		frame.setVisible( true );
	}
}
