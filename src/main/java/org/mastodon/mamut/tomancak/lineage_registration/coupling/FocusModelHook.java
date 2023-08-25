package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;

/**
 * <p>
 * A {@link SpotHook} that warps around a {@link FocusModel}.
 * </p>
 * <p>
 * This class allows to use the {@link SpotHook} interface to set the focused
 * spot and to listen to changes of the focused spot in the {@link FocusModel}.
 * </p>
 * <p>
 * The constructor registers the {@link FocusModelHook} as a listener to the
 * {@link FocusModel}. The listener can be removed by calling {@link #close()}.
 * </p>
 */
class FocusModelHook implements SpotHook, FocusListener
{

	private final ModelGraph graph;

	private final FocusModel< Spot > focusModel;

	private Consumer< Spot > listener;

	public FocusModelHook( ProjectModel model )
	{
		this.graph = model.getModel().getGraph();
		this.focusModel = model.getFocusModel();
		focusModel.listeners().add( this );
	}

	@Override
	public void set( Spot spot )
	{
		focusModel.focusVertex( spot );
	}

	@Override
	public void setListener( Consumer< Spot > listener )
	{
		this.listener = listener;
	}

	@Override
	public void close()
	{
		focusModel.listeners().remove( this );
	}

	@Override
	public void focusChanged()
	{
		final Spot ref = graph.vertexRef();
		try
		{
			final Spot spot = focusModel.getFocusedVertex( ref );
			if ( listener != null )
				listener.accept( spot );
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}
}
