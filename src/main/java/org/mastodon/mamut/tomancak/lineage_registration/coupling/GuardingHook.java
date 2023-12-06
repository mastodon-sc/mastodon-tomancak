/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.mamut.model.Spot;

/**
 * <p>
 * The {@link GuardingHook} is a simple wrapper around a {@link SpotHook} that
 * can be used to break loops when realizing a bidirectional coupling between two
 * {@link SpotHook}s.
 * </p>
 * <p>
 * A bidirectional coupling between two {@link SpotHook}s can easily be achieved
 * calling {@link SpotHook#set(Spot)} in the listener of the other spot hook and
 * vice versa. But this often creates an infinite loop, because the
 * "set" method triggers the listener and the listener calls "set" method and
 * this easily goes on forever.
 * </p>
 * <p>
 * The {@link GuardingHook} can be used to break this loop. It has a guarding flag
 * {@link #guard}. The deactivates the listener while executing the "set" method
 * and vice versa.
 * </p>
 */
class GuardingHook implements SpotHook
{

	private final SpotHook parent;

	private Consumer< Spot > listener;

	private boolean guard = false;

	public GuardingHook( SpotHook parent )
	{
		this.parent = parent;
		parent.setListener( spot -> {
			if ( guard )
				return;
			guard = true;
			try
			{
				listener.accept( spot );
			}
			finally
			{
				guard = false;
			}
		} );
	}

	@Override
	public void set( Spot spot )
	{
		if ( guard )
			return;
		guard = true;
		try
		{
			parent.set( spot );
		}
		finally
		{
			guard = false;
		}
	}

	@Override
	public void setListener( Consumer< Spot > listener )
	{
		this.listener = listener;
	}

	@Override
	public void close()
	{
		parent.close();
	}
}
