package org.mastodon.mamut.tomancak.lineage_registration;

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
