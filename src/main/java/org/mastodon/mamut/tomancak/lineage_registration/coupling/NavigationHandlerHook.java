package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;

/**
 * <p>
 * A {@link SpotHook} that wraps around a {@link NavigationHandler}.
 * </p>
 * <p>
 * This class allows to use the {@link SpotHook} interface to navigate to a
 * {@link Spot} and to listen for {@link NavigationListener#navigateToVertex}
 * events.
 * </p>
 * <p>
 * The constructor registers the {@link NavigationHandlerHook} as a listener to
 * the {@link NavigationHandler}. The listener can be removed by calling
 * {@link #close()}.
 * </p>
 */
class NavigationHandlerHook implements SpotHook, NavigationListener< Spot, Link >
{

	private final NavigationHandler< Spot, Link > navigationHandler;

	private Consumer< Spot > listener;

	public NavigationHandlerHook( NavigationHandler< Spot, Link > navigationHandler )
	{
		this.navigationHandler = navigationHandler;
		navigationHandler.listeners().add( this );
	}

	@Override
	public void set( Spot spot )
	{
		navigationHandler.notifyNavigateToVertex( spot );
	}

	@Override
	public void setListener( Consumer< Spot > listener )
	{
		this.listener = listener;
	}

	@Override
	public void close()
	{
		navigationHandler.listeners().remove( this );
	}

	@Override
	public void navigateToVertex( Spot vertex )
	{
		if ( listener != null )
			listener.accept( vertex );
	}

	@Override
	public void navigateToEdge( Link edge )
	{

	}
}
