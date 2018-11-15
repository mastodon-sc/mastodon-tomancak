package org.mastodon.tomancak;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.UIManager;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.mamut.Mastodon;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = TomancakPlugins.class )
public class TomancakPlugins extends AbstractContextual implements MastodonPlugin
{
	private static final String EXPORT_PHYLOXML = "[tomancak] export phyloxml for selection";

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( EXPORT_PHYLOXML, "Export phyloXML for selection" );
	}

	private final AbstractNamedAction exportPhyloXmlAction;

	private MastodonPluginAppModel pluginAppModel;

	public TomancakPlugins()
	{
		exportPhyloXmlAction = new RunnableAction( EXPORT_PHYLOXML, this::exportPhyloXml );
		updateEnabledActions();
	}

	@Override
	public void setAppModel( final MastodonPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins",
						menu( "Tomancak lab",
								item( EXPORT_PHYLOXML ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportPhyloXmlAction );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		exportPhyloXmlAction.setEnabled( appModel != null );
	}

	private void exportPhyloXml()
	{
		if ( pluginAppModel != null )
			MakePhyloXml.exportSelectedSubtreeToPhyloXmlFile( pluginAppModel.getAppModel() );
	}

	/*
	 * Start Mastodon ...
	 */

	public static void main( final String[] args ) throws Exception
	{
		final String projectPath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/1.SimView2_20130315_Mastodon_Automat-segm-t0-t300";

		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Mastodon mastodon = new Mastodon();
		new Context().inject( mastodon );
		mastodon.run();

		final MamutProject project = new MamutProjectIO().load( projectPath );
		mastodon.openProject( project );
	}
}
