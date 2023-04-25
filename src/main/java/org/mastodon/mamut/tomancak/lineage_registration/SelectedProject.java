package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;

/**
 * Used in {@link LineageRegistrationFrame} and {@link LineageRegistrationControlService}
 * to exchange information about the selected projects and related sittings.
 */
public class SelectedProject
{

	private final String name;

	private final WindowManager windowManager;

	private final int firstTimepoint;

	public SelectedProject( WindowManager windowManager, String name, int firstTimepoint )
	{
		this.windowManager = windowManager;
		this.name = name;
		this.firstTimepoint = firstTimepoint;
	}

	public String getName()
	{
		return name;
	}

	public WindowManager getWindowManager()
	{
		return windowManager;
	}

	public int getFirstTimepoint()
	{
		return firstTimepoint;
	}

	public MamutAppModel getAppModel()
	{
		return windowManager.getAppModel();
	}

	public Model getModel()
	{
		return getAppModel().getModel();
	}

	public ModelGraph getGraph()
	{
		return getModel().getGraph();
	}
}
