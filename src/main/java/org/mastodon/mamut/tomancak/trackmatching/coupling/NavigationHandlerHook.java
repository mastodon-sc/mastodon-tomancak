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
package org.mastodon.mamut.tomancak.trackmatching.coupling;

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
