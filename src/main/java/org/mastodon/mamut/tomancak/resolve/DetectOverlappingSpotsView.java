package org.mastodon.mamut.tomancak.resolve;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.tomancak.util.DefaultCancelable;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, name = "Create Conflict Tag Set" )
public class DetectOverlappingSpotsView extends DefaultCancelable implements Command
{
	private static final double STRICT_THRESHOLD = 0.2;

	private static final String STRICT = "find less conflicts (threshold = 0.2)";

	private static final double DEFAULT_THRESHOLD = 0.4;

	private static final String DEFAULT = "default (threshold = 0.4)";

	private static final double LOOSE_THRESHOLD = 0.6;

	private static final String LOOSE = "find more conflicts (threshold = 0.6)";

	private static final String CUSTOM = "Use custom threshold";

	@Parameter
	private ProjectModel projectModel;

	@Parameter( label = "Predefined Threshold:", choices = { STRICT, DEFAULT, LOOSE, CUSTOM }, style = "radioButtonVertical" )
	private String choice = DEFAULT;

	@Parameter( label = "Custom Threshold:", min = "0.0", max = "1.0", stepSize = "0.05" )
	private double customThreshold = 0.4;

	public static void run( final ProjectModel projectModel )
	{
		final CommandService cmd = projectModel.getContext().service( CommandService.class );
		cmd.run( DetectOverlappingSpotsView.class, true, "projectModel", projectModel );
	}

	@Override
	public void run()
	{
		final double threshold = getThreshold();
		final String tagSetName = String.format( "Overlapping Spots (threshold=%1.2f)", threshold );
		DetectOverlappingSpots.run( projectModel.getModel(), tagSetName, threshold );
	}

	private double getThreshold()
	{
		switch ( choice )
		{
		case STRICT:
			return STRICT_THRESHOLD;
		case DEFAULT:
			return DEFAULT_THRESHOLD;
		case LOOSE:
			return LOOSE_THRESHOLD;
		default:
			return customThreshold;
		}
	}

}
