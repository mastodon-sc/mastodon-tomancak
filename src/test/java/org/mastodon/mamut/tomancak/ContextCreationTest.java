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
package org.mastodon.mamut.tomancak;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.scijava.Context;

/**
 * Tests that the SciJava {@link Context} can be created. This ensures that there
 * is no Exception thrown when the Mastodon services are initialized.
 * <p>
 * We need to make sure that all SciJava services in Mastodon can be properly
 * initialized. This is important because otherwise Fiji will not start properly
 * when Mastodon is installed.
 * <p>
 * This test is executed in headless mode during the continuous integration
 * tests (GitHub actions). The test must pass in normal and in headless mode.
 * Because Fiji can also be started normally or in headless mode.
 *
 * @author Masthias Arzt
 */
public class ContextCreationTest
{
	@Test
	public void testCreateContext()
	{
		try (Context context = new Context())
		{
			// Make sure that the SciJava context with all the Mastodon services can be created.
			// This must work also in headless mode.
			assertNotNull( context );
		}
	}
}
