package org.mastodon.mamut.tomancak.collaboration.commands;

import org.scijava.Cancelable;

public class AbstractCancellable implements Cancelable
{

	private boolean canceled = false;

	private String reason = null;

	@Override
	public boolean isCanceled()
	{
		return canceled;
	}

	@Override
	public void cancel( String reason )
	{
		this.canceled = true;
		this.reason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return reason;
	}
}
