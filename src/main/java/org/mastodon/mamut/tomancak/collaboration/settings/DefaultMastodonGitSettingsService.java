package org.mastodon.mamut.tomancak.collaboration.settings;

import org.eclipse.jgit.lib.PersonIdent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin( type = Service.class )
public class DefaultMastodonGitSettingsService extends AbstractService implements MastodonGitSettingsService
{

	@Parameter
	private PrefService prefService;

	private String authorName;

	private String authorEmail;

	@Override
	public void initialize()
	{
		super.initialize();
		authorName = prefService.get( DefaultMastodonGitSettingsService.class, "author.name", null );
		authorEmail = prefService.get( DefaultMastodonGitSettingsService.class, "author.email", null );
	}

	@Override
	public boolean isAuthorSpecified()
	{
		return authorName != null && authorEmail != null;
	}

	@Override
	public void setAuthorName( String name )
	{
		this.authorName = name;
		prefService.put( DefaultMastodonGitSettingsService.class, "author.name", name );
	}

	@Override
	public void setAuthorEmail( String email )
	{
		this.authorEmail = email;
		prefService.put( DefaultMastodonGitSettingsService.class, "author.email", email );
	}

	@Override
	public String getAuthorName()
	{
		return authorName;
	}

	@Override
	public String getAuthorEmail()
	{
		return authorEmail;
	}

	@Override
	public PersonIdent getPersonIdent()
	{
		return new PersonIdent( authorName, authorEmail );
	}
}
