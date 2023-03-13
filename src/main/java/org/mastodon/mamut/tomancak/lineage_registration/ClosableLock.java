package org.mastodon.mamut.tomancak.lineage_registration;

/**
 * Similar to {@link AutoCloseable}, but without the checked exception.
 */
interface ClosableLock extends AutoCloseable
{
	void close();
}
