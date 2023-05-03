package org.mastodon.mamut.tomancak.lineage_registration.spacial_registration;

import org.mastodon.collection.RefRefMap;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

public interface SpacialRegistrationFactory
{

	SpacialRegistration run( Model modelA, Model modelB, RefRefMap< Spot, Spot > rootsAB );
}
