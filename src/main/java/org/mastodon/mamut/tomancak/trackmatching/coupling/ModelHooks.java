/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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

import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;

/**
 * This class provides {@link SpotHook}s to access the focused spot,
 * highlighted spot and to navigate to a spot in a {@link ProjectModel}.
 * It also provides a {@link #close()} method as an easy way to  detach
 * the hooks from the {@link ProjectModel}.
 */
class ModelHooks implements AutoCloseable
{

	private final FocusModelHook focusModelHook;

	private final HighlightModelHook highlightModelHook;

	private final NavigationHandlerHook navigationHandlerHook;

	public ModelHooks( ProjectModel model, int groupId )
	{
		this.focusModelHook = new FocusModelHook( model );
		this.highlightModelHook = new HighlightModelHook( model );
		GroupHandle groupHandle = model.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( groupId );
		this.navigationHandlerHook = new NavigationHandlerHook( groupHandle.getModel( model.NAVIGATION ) );
	}

	public SpotHook focusModelHook()
	{
		return focusModelHook;
	}

	public SpotHook highlightModelHook()
	{
		return highlightModelHook;
	}

	public SpotHook navigationHandlerHook()
	{
		return navigationHandlerHook;
	}

	@Override
	public void close()
	{
		focusModelHook.close();
		highlightModelHook.close();
		navigationHandlerHook.close();
	}
}
