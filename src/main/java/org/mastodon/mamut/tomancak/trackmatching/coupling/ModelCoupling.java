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

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.tomancak.trackmatching.RegisteredGraphs;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;

/**
 * <p>
 * This class couples two {@link ProjectModel}s together by connecting their
 * {@link FocusModel}, {@link HighlightModel} and {@link NavigationHandler}.
 * </p>
 * <p>
 * The {@link RegisteredGraphs} instance is used to map between the spots
 * of the two models.
 * </p>
 * <p>
 * It also provides a {@link #close()} method that allows to remove the
 * coupling again.
 * </p>
 */
public class ModelCoupling implements AutoCloseable
{

	private final ModelHooks hooksA;

	private final ModelHooks hooksB;

	public ModelCoupling( final ProjectModel modelA, final ProjectModel modelB, final RegisteredGraphs registeredGraphs, int groupId )
	{
		this.hooksA = new ModelHooks( modelA, groupId );
		this.hooksB = new ModelHooks( modelB, groupId );
		coupleHooks( registeredGraphs, hooksA.highlightModelHook(), hooksB.highlightModelHook() );
		coupleHooks( registeredGraphs, hooksA.focusModelHook(), hooksB.focusModelHook() );
		coupleHooks( registeredGraphs, hooksA.navigationHandlerHook(), hooksB.navigationHandlerHook() );
	}

	@Override
	public void close()
	{
		hooksA.close();
		hooksB.close();
	}

	private static void coupleHooks( RegisteredGraphs registeredGraphs, SpotHook a, SpotHook b )
	{
		SpotHook ga = new GuardingHook( a );
		SpotHook gb = new GuardingHook( b );
		new BranchMapCoupling( ga, gb, registeredGraphs );
		new BranchMapCoupling( gb, ga, registeredGraphs.swapAB() );
	}
}
