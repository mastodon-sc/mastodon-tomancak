package org.mastodon.mamut.tomancak.lineage_registration.coupling;

import java.util.function.Consumer;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;

/**
 * <p>
 * A {@link SpotHook} that wraps around a {@link HighlightModel}.
 * </p>
 * <p>
 * This class allows to use the {@link SpotHook} interface to set the
 * highlighted spot and to and listen for changes of the highlighted spot in
 * the {@link HighlightModel}.
 * </p>
 * <p>
 * The constructor registers the {@link HighlightModelHook} as a listener to the
 * {@link HighlightModel}. The listener can be removed by calling
 * {@link #close()}.
 * </p>
 */
class HighlightModelHook implements SpotHook, HighlightListener
{

	private final ModelGraph graph;

	private final HighlightModel< Spot, Link > highlightModel;

	private Consumer< Spot > listener;

	public HighlightModelHook( MamutAppModel model )
	{
		this.graph = model.getModel().getGraph();
		this.highlightModel = model.getHighlightModel();
		highlightModel.listeners().add( this );
	}

	@Override
	public void set( Spot spot )
	{
		if ( spot == null )
			highlightModel.clearHighlight();
		else
			highlightModel.highlightVertex( spot );
	}

	@Override
	public void setListener( Consumer< Spot > listener )
	{
		this.listener = listener;
	}

	@Override
	public void close()
	{
		highlightModel.listeners().remove( this );
	}

	@Override
	public void highlightChanged()
	{
		final Spot ref = graph.vertexRef();
		try
		{
			final Spot spot = highlightModel.getHighlightedVertex( ref );
			if ( listener != null )
				listener.accept( spot );
		}
		finally
		{
			graph.releaseRef( ref );
		}
	}
}
