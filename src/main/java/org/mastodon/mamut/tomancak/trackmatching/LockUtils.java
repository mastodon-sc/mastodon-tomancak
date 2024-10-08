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
package org.mastodon.mamut.tomancak.trackmatching;

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
