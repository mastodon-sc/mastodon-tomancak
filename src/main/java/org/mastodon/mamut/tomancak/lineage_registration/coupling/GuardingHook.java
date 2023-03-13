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
