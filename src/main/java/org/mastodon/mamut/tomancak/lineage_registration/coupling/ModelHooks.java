package org.mastodon.mamut.tomancak.lineage_registration.coupling;

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
