package org.mastodon.mamut.tomancak.lineage_registration.spatial_registration;

import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

public interface SpatialRegistrationFactory
{

	SpatialRegistration run( Model modelA, Model modelB, RefRefMap< Spot, Spot > rootsAB );
}
