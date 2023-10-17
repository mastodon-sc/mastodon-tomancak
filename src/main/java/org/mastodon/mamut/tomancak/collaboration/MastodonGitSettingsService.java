package org.mastodon.mamut.tomancak.collaboration;

import org.eclipse.jgit.lib.PersonIdent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

@Plugin( type = SciJavaService.class )
public class MastodonGitSettingsService extends AbstractService
{

	@Parameter
	private PrefService prefService;

	private String authorName;

	private String authorEmail;

	@Override
	public void initialize()
	{
		super.initialize();
		authorName = prefService.get( MastodonGitSettingsService.class, "author.name", null );
		authorEmail = prefService.get( MastodonGitSettingsService.class, "author.email", null );
	}

	public boolean isAuthorSpecified()
	{
		return authorName != null && authorEmail != null;
	}

	public void setAuthorName( String name )
	{
		this.authorName = name;
		prefService.put( MastodonGitSettingsService.class, "author.name", name );
	}

	public void setAuthorEmail( String email )
	{
		this.authorEmail = email;
		prefService.put( MastodonGitSettingsService.class, "author.email", email );
	}

	public String getAuthorName()
	{
		return authorName;
	}

	public String getAuthorEmail()
	{
		return authorEmail;
	}

	public PersonIdent getPersonIdent()
	{
		return new PersonIdent( authorName, authorEmail );
	}
}
