/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2023 Vladimir Ulman
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

import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.tomancak.util.SpotsIterator;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mastodon.mamut.tomancak.lineage_registration.TagSetUtils.rgbToValidColor;

@Plugin( type = Command.class, name = "Random colorize lineages" )
public class LineageRandomColorizer extends DynamicCommand  {
	@Parameter(persist = false)
	private MamutPluginAppModel pluginAppModel;

	@Parameter
	private LogService logService;

	@Parameter(label = "Choose color scheme:", initializer = "readAvailColors", choices = {})
	private String colorScheme = "Create new tagset";

	final static private String COLORS_16 = "Create and use a new tagset (16 colors)";
	final static private String COLORS_64 = "Create and use a new tagset (64 colors)";

	// sets up the drop-down box in the dialog with two create-tagset-items
	// followed by the list of the currently available tagsets
	private void readAvailColors() {
		List<String> choices = new ArrayList<>(50);

		pluginAppModel.getAppModel().getModel()
				.getTagSetModel()
				.getTagSetStructure()
				.getTagSets()
				.forEach( ts -> choices.add( "Use existing "+ts.getName() ) );

		//finally, also allow to create new tag sets (color palettes)
		choices.add( COLORS_16 );
		choices.add( COLORS_64 );

		this.getInfo().getMutableInput( "colorScheme", String.class ).setChoices( choices );
	}

	static final Map<String, Integer> mokolePaletteOf16Colors = new HashMap<>(16);
	{
		// used https://mokole.com/palette.html with the settings:
		// 16 colors, 5% min luminosity, 90% max luminosity, 5000 max loops
		mokolePaletteOf16Colors.put( "darkslategray", rgbToValidColor(0x2f4f4f) );
		mokolePaletteOf16Colors.put( "saddlebrown", rgbToValidColor(0x8b4513) );
		mokolePaletteOf16Colors.put( "darkgreen", rgbToValidColor(0x006400) );
		mokolePaletteOf16Colors.put( "darkkhaki", rgbToValidColor(0xbdb76b) );
		mokolePaletteOf16Colors.put( "navy", rgbToValidColor(0x000080) );
		mokolePaletteOf16Colors.put( "mediumturquoise", rgbToValidColor(0x48d1cc) );
		mokolePaletteOf16Colors.put( "red", rgbToValidColor(0xff0000) );
		mokolePaletteOf16Colors.put( "orange", rgbToValidColor(0xffa500) );
		mokolePaletteOf16Colors.put( "yellow", rgbToValidColor(0xffff00) );
		mokolePaletteOf16Colors.put( "lime", rgbToValidColor(0x00ff00) );
		mokolePaletteOf16Colors.put( "mediumspringgreen", rgbToValidColor(0x00fa9a) );
		mokolePaletteOf16Colors.put( "blue", rgbToValidColor(0x0000ff) );
		mokolePaletteOf16Colors.put( "orchid", rgbToValidColor(0xda70d6) );
		mokolePaletteOf16Colors.put( "fuchsia", rgbToValidColor(0xff00ff) );
		mokolePaletteOf16Colors.put( "dodgerblue", rgbToValidColor(0x1e90ff) );
		mokolePaletteOf16Colors.put( "lightpink", rgbToValidColor(0xffb6c1) );
	}

	private TagSetStructure.TagSet createCoolSmallTagSet() {
		TagSetStructure.TagSet tSet = pluginAppModel
				.getAppModel()
				.getModel()
				.getTagSetModel()
				.getTagSetStructure()
				.createTagSet( "Palette of 16 colors" );

		mokolePaletteOf16Colors.forEach(tSet::createTag);
		return tSet;
	}


	private TagSetStructure.TagSet createNewTagSet(final int rColors,
	                                               final int gColors,
	                                               final int bColors) {
		TagSetStructure.TagSet tSet = pluginAppModel
				.getAppModel()
				.getModel()
				.getTagSetModel()
				.getTagSetStructure()
				.createTagSet( "Palette of "+(rColors*gColors*bColors)+" colors" );

		final int rStep = 256 / rColors; //relying on down-rounding
		final int gStep = 256 / gColors;
		final int bStep = 256 / bColors;

		for (int rColor = 0; rColor < rColors; ++rColor)
			for (int gColor = 0; gColor < gColors; ++gColor)
				for (int bColor = 0; bColor < bColors; ++bColor) {
					int color = ((rColor*rStep) << 16) + ((gColor*gStep) << 8) + bColor*bStep;
					tSet.createTag("RGB "+(rColor*rStep)+","
							+(gColor*gStep)+","+(bColor*bStep), rgbToValidColor(color) );
				}
		return tSet;
	}


	private TagSetStructure.TagSet getChosenTagSet() {
		TagSetStructure.TagSet chosenTagSet = null;

		if (colorScheme.equals(COLORS_16)) chosenTagSet = createCoolSmallTagSet();
		else if (colorScheme.equals(COLORS_64)) chosenTagSet = createNewTagSet(4,4,4);
		else {
			Optional<TagSetStructure.TagSet> ts = pluginAppModel.getAppModel().getModel()
					.getTagSetModel()
					.getTagSetStructure()
					.getTagSets()
					.stream()
					.filter(_ts -> _ts.getName().equals(colorScheme))
					.findFirst();
			if (!ts.isPresent())
				throw new IllegalStateException("Requested tagset '"+colorScheme+"' was not found now.");
			chosenTagSet = ts.get();
		}

		return chosenTagSet;
	}


	public void run()
	{
		pluginAppModel.getAppModel().getModel().getTagSetModel().pauseListeners();

		final TagSetStructure.TagSet chosenTagSet = getChosenTagSet();
		AtomicInteger currentColorIdx = new AtomicInteger( 0 );

		final ObjTags<Spot> colorizer = pluginAppModel.getAppModel().getModel().getTagSetModel().getVertexTags();
		final Logger dedicatedLog = logService.subLogger( "Coloring of lineage trees" );

		final SpotsIterator visitor = new SpotsIterator( pluginAppModel.getAppModel(), dedicatedLog );
		visitor.visitRootsFromEntireGraph( root -> {
			TagSetStructure.Tag color = chosenTagSet.getTags().get( currentColorIdx.get() );
			currentColorIdx.set( (currentColorIdx.get()+1) % chosenTagSet.getTags().size() );

			visitor.visitDownstreamSpots( root, spot -> colorizer.set( spot, color ) );
		} );
		dedicatedLog.info("Done with the random coloring.");

		pluginAppModel.getAppModel().getModel().getTagSetModel().resumeListeners();
	}
}