package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.mamut.model.Spot;

/**
 * The {@link SpotHook} is a simple interface that allows to set a spot and to
 * listen to spot changes.
 */
public interface SpotHook extends AutoCloseable
{
	void set( Spot spot );

	void setListener( Consumer< Spot > listener );

	@Override
	void close();
}
