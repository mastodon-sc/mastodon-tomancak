package org.mastodon.mamut.tomancak.lineage_registration;

import java.util.concurrent.locks.Lock;

/**
 * Utility class for working with {@link Lock}s.
 */
public class LockUtils
{
	private LockUtils()
	{
		// prevent utility class instantiation
	}

	/**
	 * Acquires two {@link Lock}s in a deadlock-free way. Returns an instance
	 * of {@link ClosableLock} that unlocks both locks when closed.
	 * <p>
	 * This method is ideally used in a try-with-resources block:
	 * <pre>
	 * try( ClosableLock l = LockUtils.lockBoth( lockA, lockB ) ) {
	 *    // perform operations that require both locks to be set
	 * }
	 * </pre>
	 */
	public static ClosableLock lockBoth( final Lock a, final Lock b )
	{
		ClosableLock closableLock = () -> {
			a.unlock();
			b.unlock();
		};
		while ( true )
		{
			a.lock();
			if ( b.tryLock() )
				return closableLock;
			else
				a.unlock();
			b.lock();
			if ( a.tryLock() )
				return closableLock;
			else
				b.unlock();
		}
	}
}
