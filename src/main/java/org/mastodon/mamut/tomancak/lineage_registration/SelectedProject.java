package org.mastodon.mamut.tomancak.lineage_registration;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;

/**
 * Used in {@link LineageRegistrationFrame} and {@link LineageRegistrationControlService}
 * to exchange information about the selected projects and related sittings.
 */
public class SelectedProject
{

	private final String name;

	private final int firstTimepoint;

	private final ProjectModel projectModel;

	public SelectedProject( ProjectModel projectModel, String name, int firstTimepoint )
	{
		this.projectModel = projectModel;
		this.name = name;
		this.firstTimepoint = firstTimepoint;
	}

	public String getName()
	{
		return name;
	}

	public int getFirstTimepoint()
	{
		return firstTimepoint;
	}

	public ProjectModel getProjectModel()
	{
		return projectModel;
	}

	public Model getModel()
	{
		return getProjectModel().getModel();
	}

	public ModelGraph getGraph()
	{
		return getModel().getGraph();
	}
}
