package org.mastodon.mamut.tomancak.collaboration;

public class ErrorDialogDemo
{
	public static void main( String... args )
	{
		try
		{
			throw new IllegalArgumentException( "Test exception" );
		}
		catch ( Exception e )
		{
			ErrorDialog.showErrorMessage( "Title", e );
		}
	}
}
