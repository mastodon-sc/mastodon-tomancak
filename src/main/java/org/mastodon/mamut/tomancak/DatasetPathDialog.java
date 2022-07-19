/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2021 Tobias Pietzsch
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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.mastodon.mamut.project.MamutProject;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;

public class DatasetPathDialog extends JDialog
{
	private final MamutProject project;

	public DatasetPathDialog( final Frame owner, final MamutProject project )
	{
		super( owner, "Edit Dataset Path...", false );
		this.project = project;

		final JPanel content = new JPanel();
		content.setLayout( new GridBagLayout() );
		content.setBorder( BorderFactory.createEmptyBorder( 30, 20, 20, 20 ) );

		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0.0;
		content.add( new JLabel( "Current BDV dataset path: " ), c );

		final JTextField pathTextField = new JTextField( project.getDatasetXmlFile().getAbsolutePath() );
		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		content.add( pathTextField, c );
		pathTextField.setColumns( 20 );

		final JButton browseButton = new JButton( "Browse" );
		c.gridx = 2;
		c.weightx = 0.0;
		content.add( browseButton, c );

		++c.gridy;
		c.gridx = 0;
		content.add( new JLabel( "Store as absolute path: " ), c );
		final JCheckBox storeAbsoluteCheckBox = new JCheckBox();
		storeAbsoluteCheckBox.setSelected( !project.isDatasetXmlPathRelative() );
		c.gridx = 1;
		content.add( storeAbsoluteCheckBox, c );

		final JPanel buttons = new JPanel();
		final JButton cancel = new JButton("Cancel");
		final JButton ok = new JButton("OK");
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( cancel );
		buttons.add( ok );

		getContentPane().add( content, BorderLayout.CENTER );
		getContentPane().add( buttons, BorderLayout.SOUTH );

		class Browse implements ActionListener
		{
			private final JTextField path;

			private final String dialogTitle;

			public Browse( final JTextField path, final String dialogTitle )
			{
				this.path = path;
				this.dialogTitle = dialogTitle;
			}

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final File file = FileChooser.chooseFile(
						true,
						DatasetPathDialog.this,
						path.getText(),
						new ExtensionFileFilter( "xml" ),
						dialogTitle,
						FileChooser.DialogType.LOAD,
						FileChooser.SelectionMode.FILES_ONLY );
				if ( file != null )
					path.setText( file.getAbsolutePath() );
			}
		}
		browseButton.addActionListener( new Browse( pathTextField, "Select BDV XML file" ) );

		ok.addActionListener( e -> {
			final String path = pathTextField.getText();
			final boolean relative = !storeAbsoluteCheckBox.isSelected();
			project.setDatasetXmlFile( new File( path ) );
			project.setDatasetXmlPathRelative( relative );
			close();
		} );

		cancel.addActionListener( e -> close() );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				close();
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}

	private void close()
	{
		setVisible( false );
		dispose();
	}
}
