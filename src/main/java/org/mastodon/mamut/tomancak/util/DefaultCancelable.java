package org.mastodon.mamut.tomancak.util;

import org.scijava.Cancelable;

/**
 * This class can be used as super class when implementing a SciJava {@link org.scijava.command.Command}.
 * The GUI that is created for the command will have a "Cancel" button.
 */
public class DefaultCancelable implements Cancelable
{
	private String reason;

	public void deleteCancelReason()
	{
		reason = null;
	}

	@Override
	public boolean isCanceled()
	{
		return null != reason;
	}

	@Override
	public void cancel( final String reason )
	{
		this.reason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return reason;
	}
}
