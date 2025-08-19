package org.mastodon.mamut.tomancak.lineage_registration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.tomancak.lineage_registration.spatial_registration.SpatialRegistrationMethod;
import org.mastodon.mamut.views.trackscheme.MamutBranchViewTrackScheme;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

/**
 * Do pairwise {@link LineageRegistrationAlgorithm lineage registration} of multiple embryos.
 * Tag the cells that are uniquely identified by the registration, without contradiction.
 */
public class MultiEmbryoRegistration
{
	public static void main( final String... args )
	{
		final List< String > projectPaths = Arrays.asList(
				"/home/arzt/Datasets/Mette/E1.mastodon",
				"/home/arzt/Datasets/Mette/E2.mastodon",
				"/home/arzt/Datasets/Mette/E3.mastodon"
		);
		try (final Context context = new Context())
		{
			final List< ProjectModel > projectModels = projectPaths.stream().map( path -> {
				try
				{
					return ProjectLoader.open( path, context, false, true );
				}
				catch ( IOException | SpimDataException e )
				{
					throw new RuntimeException( e );
				}
			} ).collect( Collectors.toList() );
			final List< Model > models = projectModels.stream().map( ProjectModel::getModel ).collect( Collectors.toList() );
			// FIXME: models.forEach( model -> ImproveAnglesDemo.removeBackEdges( model.getGraph() ) );
			createTags( models.get( 0 ), computeAgreement( models ) );
			projectModels.get( 0 ).getWindowManager().createView( MamutBranchViewTrackScheme.class );
		}
	}

	private static RefIntMap< Spot > computeAgreement( final List< Model > models )
	{
		final Map< Pair< Model, Model >, RefRefMap< Spot, Spot > > registrations = new HashMap<>();
		for ( final Pair< Model, Model > pair : makePairs( models ) )
			registrations.put( pair, register( pair.getLeft(), pair.getRight() ) );

		final Model firstModel = models.get( 0 );
		final RefIntMap< Spot > agreement = new RefIntHashMap<>( firstModel.getGraph().vertices().getRefPool(), 0 );

		final List< Model > otherModels = models.subList( 1, models.size() );
		for ( final Pair< Model, Model > pair : makePairs( otherModels ) )
		{
			final Model otherModelA = pair.getLeft();
			final Model otherModelB = pair.getRight();
			final RefRefMap< Spot, Spot > registrationA = registrations.get( Pair.of( firstModel, otherModelA ) );
			final RefRefMap< Spot, Spot > registrationB = registrations.get( Pair.of( firstModel, otherModelB ) );
			final RefRefMap< Spot, Spot > registrationAB = registrations.get( pair );
			incrementAgreement( agreement, registrationA, registrationB, registrationAB );
		}

		return agreement;
	}

	private static void incrementAgreement( final RefIntMap< Spot > agreement, final RefRefMap< Spot, Spot > registrationA, final RefRefMap< Spot, Spot > registrationB,
			final RefRefMap< Spot, Spot > registrationAB )
	{
		final Spot refA = registrationA.createValueRef();
		final Spot refB = registrationB.createValueRef();
		final Spot refB2 = registrationAB.createValueRef();
		for ( final Spot key : registrationA.keySet() )
			if ( registrationB.containsKey( key ) )
			{
				final Spot spotA = registrationA.get( key, refA );
				final Spot spotB = registrationB.get( key, refB );
				final Spot crossSpotB = registrationAB.get( spotA, refB2 );
				final boolean correct = spotB.equals( crossSpotB );
				if ( correct )
					agreement.put( key, agreement.get( key ) + 1 );
			}
	}

	private static RefRefMap< Spot, Spot > register( final Model firstModel, final Model otherModel )
	{
		return LineageRegistrationAlgorithm.run( firstModel, 0, otherModel, 0, SpatialRegistrationMethod.DYNAMIC_ROOTS ).mapAB;
	}

	private static List< Pair< Model, Model > > makePairs( final List< Model > otherModels )
	{
		final List< Pair< Model, Model > > spokes = new ArrayList<>();
		for ( int i = 0; i < otherModels.size(); i++ )
			for ( int j = i + 1; j < otherModels.size(); j++ )
				spokes.add( Pair.of( otherModels.get( i ), otherModels.get( j ) ) );
		return spokes;
	}

	private static void createTags( final Model firstModel, final RefIntMap< Spot > agreement )
	{
		int max = 0;
		for ( final Spot spot : agreement.keySet() )
			max = Math.max( max, agreement.get( spot ) );
		final List< Pair< String, Integer > > correct =
				IntStream.rangeClosed( 0, max ).mapToObj( i -> Pair.of( String.valueOf( i ), Glasbey.GLASBEY[ i + 1 ] ) ).collect( Collectors.toList() );

		final TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( firstModel, "UnifiedEmbryo", correct );
		final List< TagSetStructure.Tag > tags = tagSet.getTags();
		final ObjTagMap< Link, TagSetStructure.Tag > edgeTags = firstModel.getTagSetModel().getEdgeTags().tags( tagSet );
		for ( final Spot spot : agreement.keySet() )
		{
			final int value = agreement.get( spot );
			final TagSetStructure.Tag tag = tags.get( max - value );
			TagSetUtils.tagBranch( firstModel, tagSet, tag, spot );
			if ( spot.incomingEdges().size() == 1 )
			{
				final Link link = spot.incomingEdges().iterator().next();
				edgeTags.set( link, tag );
			}
		}
	}
}
