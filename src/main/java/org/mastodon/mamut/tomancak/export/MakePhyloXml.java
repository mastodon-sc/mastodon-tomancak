/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.tomancak.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.graph.branch.BranchGraphImp;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.pool.attributes.IntAttributeValue;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.XmlFileFilter;

import mpicbg.spim.data.XmlHelpers;

// http://www.phyloxml.org/
public class MakePhyloXml
{
	public static void exportSelectedSubtreeToPhyloXmlFile( final MamutAppModel appModel )
	{
		final ModelGraph graph = appModel.getModel().getGraph();

		final RefSet< Spot > vertices = appModel.getSelectionModel().getSelectedVertices();
		if ( vertices.size() != 1 )
		{
			final String message;
			if ( vertices.isEmpty() )
				message = "No spot selected. (Please select the root of the subtree to export.)";
			else
				message = "Too many spots selected. (Please select only the root spot of the subtree to export.)";
			JOptionPane.showMessageDialog( null,
					message,
					"Warning",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final Spot root = vertices.iterator().next();

		final String filename = root.getLabel() + "_phylo.xml";
		final File file = FileChooser.chooseFile(
				null,
				filename,
				new XmlFileFilter(),
				"Export PhyloXML",
				FileChooser.DialogType.SAVE );
		if ( file == null )
			return;

		try
		{
			final Document doc = new MakePhyloXml( graph, root ).toXml();
			final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
			final OutputStream os = new FileOutputStream( file );
			xout.output( doc, os );
			os.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private final ModelGraph graph;

	private final Spot root;

	private final IntPropertyMap< BranchEdge > lengths;

	private final ObjPropertyMap< BranchVertex, String > labels;

	private final BranchGraph branchGraph;

	private final BranchVertex branchGraphRoot;

	public MakePhyloXml( final ModelGraph graph, final Spot root ) throws IOException
	{
		this.graph = graph;
		this.root = root;

		final ModelGraph subgraph = new ModelGraph();
		final Spot vref1 = subgraph.vertexRef();
		final Spot vref2 = subgraph.vertexRef();
		final Spot vref3 = subgraph.vertexRef();
		final Spot vref4 = subgraph.vertexRef();
		final Link eref = subgraph.edgeRef();
		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];

		final DepthFirstIterator< Spot, Link > iter = new DepthFirstIterator<>( root, graph );
		final RefRefMap< Spot, Spot > spotToSubgraph = RefMaps.createRefRefMap( graph.vertices(), subgraph.vertices() );
		while ( iter.hasNext() )
		{
			final Spot spot = iter.next();
			spot.localize( pos );
			spot.getCovariance( cov );
			final Spot sgspot = subgraph.addVertex( vref1 ).init( spot.getTimepoint(), pos, cov );
			sgspot.setLabel( spot.getLabel() );
			spotToSubgraph.put( spot, sgspot, vref2 );
			for ( final Link inedge : spot.incomingEdges() )
			{
				final Spot sgparent = spotToSubgraph.get( inedge.getSource( vref3 ), vref4 );
				if ( sgparent != null )
					subgraph.addEdge( sgparent, sgspot, eref ).init();
			}
		}
		final Spot subgraphRoot = spotToSubgraph.get( root );

//		final TreeOutputter< Spot, Link > tsto = new TreeOutputter<>( subgraph, Spot::getLabel );
//		System.out.println( tsto.get( subgraphRoot ) );

		branchGraph = new BranchGraph( subgraph );
		branchGraphRoot = branchGraph.getBranchVertex( subgraphRoot, branchGraph.vertexRef() );

		labels = new ObjPropertyMap<>( branchGraph.vertices().getRefPool() );
		for ( final BranchEdge be : branchGraph.edges() )
		{
			final Link edge = branchGraph.getLinkedEdge( be, eref );
			final Spot source = edge.getSource( vref1 );
			final Spot target = edge.getTarget( vref2 );
			if( source.equals( subgraphRoot ) )
			{
				labels.set( be.getSource(), source.getLabel() );
				labels.set( be.getTarget(), source.getLabel() );
			}
			else
			{
				labels.set( be.getTarget(), target.getLabel() );
			}
		}

		lengths = new IntPropertyMap< BranchEdge >( branchGraph.edges().getRefPool(), -1 );
		for ( final BranchEdge be : branchGraph.edges() )
		{
			Link edge = branchGraph.getLinkedEdge( be, eref );
			int length = 1;
			Spot target = edge.getTarget( vref1 );
			while ( target.outgoingEdges().size() == 1 )
			{
				++length;
				edge = target.outgoingEdges().get( 0, eref );
				target = edge.getTarget( vref1 );
			}
			lengths.set( be, length );
		}

//		final TreeOutputter< BranchVertex, BranchEdge > btsto = new TreeOutputter<>( branchGraph, bv -> {
//			int l = bv.incomingEdges().size() == 0
//					? 0
//					: lengths.get( bv.incomingEdges().iterator().next() );
//			return labels.get( bv ) + "(" + l + ")";
//		} );
//		System.out.println( btsto.get( branchGraphRoot ) );
	}

	public Document toXml()
	{
		final Namespace namespace = Namespace.getNamespace( "http://www.phyloxml.org" );
		final Element root = new Element( "phyloxml", namespace );
		final Namespace xsi = Namespace.getNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
		root.addNamespaceDeclaration( xsi );
		root.setAttribute( "schemaLocation", "http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd", xsi );

		final Element phylogeny = new Element( "phylogeny" );
		phylogeny.setAttribute( "rooted", "true" );
		phylogeny.addContent( XmlHelpers.textElement( "name", labels.get( branchGraphRoot ) ) );
		if ( branchGraphRoot.outgoingEdges().size() > 1 )
		{
			final Element clade = new Element( "clade" );
			clade.addContent( XmlHelpers.textElement( "name", labels.get( branchGraphRoot ) ) );
			clade.addContent( XmlHelpers.intElement( "branch_length", 0 ) );
			for ( final BranchEdge outgoing : branchGraphRoot.outgoingEdges() )
				clade.addContent( toXml( outgoing ) );
			phylogeny.addContent( clade );
		}
		else
		{
			phylogeny.addContent( toXml( branchGraphRoot.outgoingEdges().get( 0 ) ) );
		}
		root.addContent( phylogeny );

		return new Document( root );
	}

	private Element toXml( final BranchEdge be )
	{
		final Element clade = new Element( "clade" );

		final BranchVertex target = be.getTarget();
		clade.addContent( XmlHelpers.textElement( "name", labels.get( target ) ) );
		clade.addContent( XmlHelpers.intElement( "branch_length", lengths.get( be ) ) );
		for ( final BranchEdge outgoing : target.outgoingEdges() )
			clade.addContent( toXml( outgoing ) );

		return clade;
	}

	static class BranchGraph extends BranchGraphImp<
			Spot,
			Link,
			BranchVertex,
			BranchEdge,
			BranchVertexPool,
			BranchEdgePool,
			ByteMappedElement >
	{
		public BranchGraph( final ListenableGraph< Spot, Link > graph )
		{
			super( graph, new BranchEdgePool( 50, new BranchVertexPool( 50 ) ) );
		}

		@Override
		public BranchVertex init( final BranchVertex bv, final Spot v )
		{
			return bv.init( v.getInternalPoolIndex(), v.getTimepoint() );
		}

		@Override
		public BranchEdge init( final BranchEdge be, final Link e )
		{
			return be.init();
		}

	}

	static class BranchEdge extends AbstractListenableEdge< BranchEdge, BranchVertex, BranchEdgePool, ByteMappedElement >
	{
		protected BranchEdge( final BranchEdgePool pool )
		{
			super( pool );
		}

		public BranchEdge init()
		{
			initDone();
			return this;
		}
	}

	static class BranchEdgePool extends AbstractListenableEdgePool< BranchEdge, BranchVertex, ByteMappedElement >
	{
		public BranchEdgePool( final int initialCapacity, final BranchVertexPool vertexPool )
		{
			super( initialCapacity, AbstractEdgePool.layout, BranchEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
		}

		@Override
		protected BranchEdge createEmptyRef()
		{
			return new BranchEdge( this );
		}
	}

	static class BranchVertex
			extends AbstractListenableVertex< BranchVertex, BranchEdge, BranchVertexPool, ByteMappedElement >
			implements HasTimepoint
	{

		private final IntAttributeValue id;

		private final IntAttributeValue timepoint;

		protected BranchVertex( final BranchVertexPool pool )
		{
			super( pool );
			id = pool.id.createQuietAttributeValue( this );
			timepoint = pool.timepoint.createQuietAttributeValue( this );
		}

		public BranchVertex init( final int id, final int tp )
		{
			setId( id );
			setTimepointInternal( tp );
			initDone();
			return this;
		}


		public int getId()
		{
			return id.get();
		}

		public void setId( final int id )
		{
			this.id.set( id );
		}

		@Override
		public int getTimepoint()
		{
			return this.timepoint.get();
		}

		private void setTimepointInternal( final int tp )
		{
			this.timepoint.set( tp );
		}
	}

	static class BranchVertexPool extends AbstractListenableVertexPool< BranchVertex, BranchEdge, ByteMappedElement >
	{
		static class BranchTestVertexLayout extends AbstractVertexLayout
		{
			final IntField id = intField();
			final IntField timepoint = intField();
		}

		static BranchTestVertexLayout layout = new BranchTestVertexLayout();

		final IntAttribute< BranchVertex > id;
		final IntAttribute< BranchVertex > timepoint;

		public BranchVertexPool( final int initialCapacity )
		{
			super(
					initialCapacity,
					layout,
					BranchVertex.class,
					SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
			id = new IntAttribute<>( layout.id, this );
			timepoint = new IntAttribute<>( layout.timepoint, this );
		}

		@Override
		protected BranchVertex createEmptyRef()
		{
			return new BranchVertex( this );
		}
	}

	/*
	 * Testing...
	 */

//	public static void main( String[] args ) throws IOException
//	{
//		final String projectPath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/1.SimView2_20130315_Mastodon_Automat-segm-t0-t300";
//
//		final MamutProject project = new MamutProjectIO().load( projectPath );
//		final Model model = new Model();
//		try ( final MamutProject.ProjectReader reader = project.openForReading() )
//		{
//			model.loadRaw( reader );
//		}
//
//		final ModelGraph graph = model.getGraph();
//		final RefPool< Spot > vpool = graph.vertices().getRefPool();
//		final Spot root = vpool.getObject( 23728, vpool.createRef() );
//
//		final Document doc = new MakePhyloXml( graph, root ).toXml();
//		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
//		final OutputStream os = System.out;
//		xout.output( doc, os );
//	}
}
