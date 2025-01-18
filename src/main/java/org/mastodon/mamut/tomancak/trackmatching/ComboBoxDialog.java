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
package org.mastodon.mamut.tomancak.trackmatching;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JOptionPane;

import net.imglib2.util.Cast;

public class ComboBoxDialog
{

	public static < T > T showComboBoxDialog( Component parent, String title, String message, List< T > options,
			Function< T, String > toString )
	{
		Choice[] choices = options.stream()
				.map( option -> new Choice( toString.apply( option ), option ) )
				.toArray( Choice[]::new );
		Choice choice = ( Choice ) JOptionPane.showInputDialog( parent, message, title,
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[ 0 ] );
		return choice != null ? Cast.unchecked( choice.getValue() ) : null;
	}

	private static class Choice
	{
		private final String name;

		private final Object value;

		public Choice( String name, Object value )
		{
			this.name = name;
			this.value = value;
		}

		public Object getValue()
		{
			return value;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public static void main( String... args )
	{
		// NB: Small demo method
		List< String > options = Arrays.asList( "a", "b", "c" );
		String choice = showComboBoxDialog( null, "title", "message", options, a -> "choose: " + a );
		System.out.println( choice );
	}
}
