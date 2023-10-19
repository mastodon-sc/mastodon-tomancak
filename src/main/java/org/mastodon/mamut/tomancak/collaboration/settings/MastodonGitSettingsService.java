package org.mastodon.mamut.tomancak.collaboration.settings;

import net.imagej.ImageJService;

import org.eclipse.jgit.lib.PersonIdent;
import org.scijava.service.Service;

public interface MastodonGitSettingsService extends ImageJService
{
	boolean isAuthorSpecified();

	void setAuthorName( String name );

	void setAuthorEmail( String email );

	String getAuthorName();

	String getAuthorEmail();

	PersonIdent getPersonIdent();
}
