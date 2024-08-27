package org.mastodon.mamut.tomancak.resolve;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.tomancak.util.DefaultCancelable;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * The GUI command to create a conflict tag set.
 */
@Plugin( type = Command.class, name = "Create Conflict Tag Set" )
public class CreateConflictTagSetCommand extends DefaultCancelable implements Command
{
	/**
	 * Only spheres that strongly overlap are considered to be in conflict.
	 */
	private static final double FEW_CONFLICTS_THRESHOLD = 0.3;

	private static final String FEW_CONFLICTS = "Find less conflicts (threshold = 0.3)";

	private static final double DEFAULT_THRESHOLD = 0.5;

	private static final String DEFAULT = "Default (threshold = 0.5)";

	/**
	 * Two spheres that slightly touch are considered to be in conflict.
	 */
	private static final double MANY_CONFLICTS_THRESHOLD = 0.7;

	private static final String MANY_CONFLICTS = "Find more conflicts (threshold = 0.7)";

	private static final String CUSTOM = "Use custom threshold";

	@Parameter( visibility = ItemVisibility.MESSAGE )
	private final String description = "<html>"
			+ "\n<body width=15cm align=left>"
			+ "\n<h3>Create Conflict Tag Set</h3>"
			+ "\n<p>"
			+ "\nSearches for conflict / overlap between spots in the tracking data."
			+ "\n</p>"
			+ "\n<p>"
			+ "\nCreates a new tag set with the name \"Conflicting Spots\" that contains"
			+ "\ntags for all the detected conflicts."
			+ "\n</p>"
			+ "\n<p>"
			+ "\nTwo spots are considered to be in conflict if they overlap to a certain degree."
			+ "\nMore precisely, the Hellinger distance"
			+ "<sup><a href=\"https://en.wikipedia.org/wiki/Hellinger_distance\">1</a>,"
			+ "<a href=\"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3582582/figure/F11/\">2</a></sup>"
			+ " between the ellipsoids of the spots is computed."
			+ "\nThe spots are considered to be in conflict, if the distance is below the given threshold."
			+ "\n</p>"
			+ "\n</body>"
			+ "\n</html>";


	@Parameter
	private ProjectModel projectModel;

	@Parameter( label = "Predefined Threshold:", choices = { FEW_CONFLICTS, DEFAULT, MANY_CONFLICTS, CUSTOM }, style = "radioButtonVertical" )
	private String choice = DEFAULT;

	@Parameter( label = "Custom Threshold:", min = "0.0", max = "1.0", stepSize = "0.05" )
	private double customThreshold = 0.4;

	public static void run( final ProjectModel projectModel )
	{
		final CommandService cmd = projectModel.getContext().service( CommandService.class );
		cmd.run( CreateConflictTagSetCommand.class, true, "projectModel", projectModel );
	}

	@Override
	public void run()
	{
		final double threshold = getThreshold();
		final String tagSetName = String.format( "Conflicting Spots (threshold=%1.2f)", threshold );
		final TagSetStructure.TagSet tagSet = CreateConflictTagSet.run( projectModel.getModel(), tagSetName, threshold );
		LocateTagsFrame.run( projectModel, tagSet );
	}

	private double getThreshold()
	{
		switch ( choice )
		{
		case FEW_CONFLICTS:
			return FEW_CONFLICTS_THRESHOLD;
		case DEFAULT:
			return DEFAULT_THRESHOLD;
		case MANY_CONFLICTS:
			return MANY_CONFLICTS_THRESHOLD;
		default:
			return customThreshold;
		}
	}

}
