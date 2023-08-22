package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.tomancak.lineage_registration.RegisteredGraphs;
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
