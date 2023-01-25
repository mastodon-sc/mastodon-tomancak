package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;
import java.util.Collections;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.mastodon.blender.BlenderController;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.Context;

public class LineageRegistrationDemo
{

	private final Context context;

	public final MamutAppModel embryoA;

	public final MamutAppModel embryoB;

	public static void main(String... args) {
		new LineageRegistrationDemo().run();
	}

	private LineageRegistrationDemo()
	{
		this.context = new Context();
		this.embryoA = openAppModel( context, "/home/arzt/Datasets/Mette/E1.mastodon" );
		this.embryoB = openAppModel( context, "/home/arzt/Datasets/Mette/E2.mastodon" );
	}

	public void run()
	{
		LineageColoring.tagLineages( embryoA.getModel(), embryoB.getModel() );
		LineageRegistrationAlgorithm.run( embryoA.getModel(), embryoB.getModel() );
		addNotMappedTag();
		colorPositionCorrectness();
	}

	private void colorPositionCorrectness()
	{
		ModelGraph graphA = embryoA.getModel().getGraph();
		ModelGraph graphB = embryoB.getModel().getGraph();
		RefRefMap< Spot, Spot > roots = RootsPairing.pairRoots( graphA, graphB );
		AffineTransform3D transformAB = EstimateTransformation.estimateScaleRotationAndTranslation( roots );
		RefRefMap< Spot, Spot > mapping = new LineageRegistrationAlgorithm(
				graphA, graphB,
				roots, transformAB ).getMapping();
		BlenderController blender = new BlenderController( context, embryoA );
		double sizeA = sizeEstimate( graphA );
		double[] positionB = new double[3];
		double[] positionA = new double[3];
		Spot ref = graphB.vertexRef();
		blender.sendColors( spotA -> {
			Spot spotB = mapping.get( spotA, ref );
			if( spotB == null )
				return 0xff550000;
			spotA.localize( positionA );
			spotB.localize( positionB );
			transformAB.applyInverse( positionB, positionB );
			double distance = LinAlgHelpers.distance( positionA, positionB );
			return gray( 1 - distance / sizeA * 5 );
		} );
	}

	private int gray( double b )
	{
		if ( b > 1 )
			b = 1;
		if ( b < 0 )
			b = 0;
		int gray = (int) (b * 255);
		int color = 0xff000000 + gray + (gray << 8) + (gray << 16);
		return color;
	}

	private double sizeEstimate( ModelGraph graphB )
	{
		RealInterval boundingBox = boundingBox( graphB.vertices());
		return LinAlgHelpers.distance( boundingBox.minAsDoubleArray(), boundingBox.maxAsDoubleArray() );
	}

	private void addNotMappedTag()
	{
		RefRefMap< Spot, Spot > mapping = getMapping();
		TagSetStructure.TagSet tagSet = LineageColoring.createTagSet( embryoA.getModel(), "registration", Collections.singletonMap( "not mapped", 0xffff2222 ) );
		TagSetStructure.Tag tag = tagSet.getTags().get( 0 );
		ModelGraph graphA = embryoA.getModel().getGraph();
		for( Spot spotA : LineageTreeUtils.getBranchStarts( graphA ) ) {
			Spot spotB = mapping.get( spotA );
			if( spotB == null )
				tagBranch( tag, embryoA.getModel(), spotA );
		}
	}

	private void tagBranch( TagSetStructure.Tag tag, Model model, Spot spotA )
	{
		ModelGraph graphA = model.getGraph();
		Spot spot = graphA.vertexRef();
		try
		{
			ObjTags< Spot > vertexTags = model.getTagSetModel().getVertexTags();
			ObjTags< Link > edgeTags = model.getTagSetModel().getEdgeTags();
			spot.refTo( spotA );
			vertexTags.set( spot, tag );
			while ( spot.outgoingEdges().size() == 1 )
			{
				Link link = spot.outgoingEdges().get( 0 );
				edgeTags.set( link, tag );
				spot = link.getTarget( spot );
				vertexTags.set( spot, tag );
			}
		}
		finally
		{
			graphA.releaseRef( spot );
		}
	}

	private void colorAccordingToPosition()
	{
		RefRefMap< Spot, Spot > mapping = getMapping();
		BlenderController blender = new BlenderController( context, embryoA );
		ModelGraph graphB = embryoB.getModel().getGraph();
		RealInterval boundingBox = boundingBox(graphB.vertices());
		double[] coords = new double[3];
		Spot ref = graphB.vertexRef();
		blender.sendColors( spotA -> {
			Spot spotB = mapping.get( spotA, ref );
			if( spotB == null )
				return 0xff000000;
			spotB.localize( coords );
			return color( boundingBox, coords );
		} );
	}

	private int color( RealInterval boundingBox, double[] coords )
	{
		return 0xff000000 | channel( boundingBox, coords, 0 ) | channel( boundingBox, coords, 1 ) << 8 | channel( boundingBox, coords, 2 ) << 16;
	}

	private int channel( RealInterval boundingBox, double[] coords, int i )
	{
		double v = ( coords[ i ] - boundingBox.realMin( i ) ) / ( boundingBox.realMax( i ) - boundingBox.realMin( i ) );
		return ( int ) ( 255 * v );
	}

	private RealInterval boundingBox( RefCollection< Spot> vertices )
	{
		double[] coords = { 0, 0, 0 };
		double[] min = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double[] max = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		for( Spot spot : vertices ) {
			spot.localize( coords );
			for ( int i = 0; i < 3; i++ )
			{
				min[i] = Math.min( min[i], coords[i] );
				max[i] = Math.max( max[i], coords[i] );
			}
		}
		return new FinalRealInterval( min, max );
	}

	private RefRefMap< Spot, Spot > getMapping()
	{
		ModelGraph graphA = embryoA.getModel().getGraph();
		ModelGraph graphB = embryoB.getModel().getGraph();
		RefRefMap< Spot, Spot > roots = RootsPairing.pairRoots( graphA, graphB );
		AffineTransform3D transformAB = EstimateTransformation.estimateScaleRotationAndTranslation( roots );
		return new LineageRegistrationAlgorithm(
				graphA, graphB,
				roots, transformAB ).getMapping();
	}

	private static MamutAppModel openAppModel( Context context, String projectPath )
	{
		try
		{
			MamutProject project = new MamutProjectIO().load( projectPath );
			WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			new MainWindow( wm ).setVisible( true );
			return wm.getAppModel();
		}
		catch ( SpimDataException | IOException e )
		{
			throw new RuntimeException(e);
		}
	}

}
