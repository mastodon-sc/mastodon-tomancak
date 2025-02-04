/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.divisioncount;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class DivisionCountChart extends JFrame
{
	DivisionCountChart( double[] xValues, double[] yValues, int windowSize )
	{
		XYSeriesCollection dataset = createDataSet( xValues, yValues, windowSize );

		JFreeChart chart =
				ChartFactory.createXYLineChart( null, "Timepoint", "Division counts", dataset, PlotOrientation.VERTICAL, true, true, false
				);
		XYPlot plot = chart.getXYPlot();

		XYLineAndShapeRenderer renderer = createDataRenderer();
		plot.setRenderer( renderer );
		setChartColors( plot );
		formatAxes( chart );

		ChartPanel panel = new ChartPanel( chart );
		panel.setPreferredSize( new Dimension( 800, 600 ) );
		setContentPane( panel );
		setTitle( "Division counts over time" );
		setSize( 800, 600 );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setLocationRelativeTo( null );
	}

	private static XYSeriesCollection createDataSet( final double[] xValues, final double[] yValues, final int windowSize )
	{
		XYSeries originalSeries = new XYSeries( "Division counts" );
		XYSeries smoothedSeries = new XYSeries( "Sliding Average" );

		for ( int i = 0; i < xValues.length; i++ )
			originalSeries.add( xValues[ i ], yValues[ i ] );

		double[] smoothedValues = calculateSlidingAverage( yValues, windowSize );
		for ( int i = 0; i < xValues.length; i++ )
			smoothedSeries.add( xValues[ i ], smoothedValues[ i ] );

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries( originalSeries );
		dataset.addSeries( smoothedSeries );
		return dataset;
	}

	private static void formatAxes( final JFreeChart chart )
	{
		NumberAxis xAxis = ( NumberAxis ) chart.getXYPlot().getDomainAxis();
		xAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
		xAxis.setNumberFormatOverride( new java.text.DecimalFormat( "#" ) );

		NumberAxis yAxis = ( NumberAxis ) chart.getXYPlot().getRangeAxis();
		yAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
		yAxis.setNumberFormatOverride( new java.text.DecimalFormat( "#" ) );
	}

	private static void setChartColors( final XYPlot plot )
	{
		plot.setBackgroundPaint( Color.WHITE );
		plot.setDomainGridlinePaint( Color.BLACK );
		plot.setRangeGridlinePaint( Color.BLACK );
	}

	private static XYLineAndShapeRenderer createDataRenderer()
	{
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		// points
		renderer.setSeriesLinesVisible( 0, false );
		renderer.setSeriesShapesVisible( 0, true );
		renderer.setSeriesPaint( 0, Color.BLUE );
		renderer.setSeriesShapesFilled( 0, true );
		renderer.setSeriesShape( 0, new java.awt.geom.Ellipse2D.Double( -2, -2, 4, 4 ) );

		// average line
		renderer.setSeriesLinesVisible( 1, true );
		renderer.setSeriesShapesVisible( 1, false );
		renderer.setSeriesPaint( 1, Color.ORANGE );
		renderer.setSeriesStroke( 1, new BasicStroke( 2.0f ) );
		return renderer;
	}

	private static double[] calculateSlidingAverage( double[] values, int windowSize )
	{
		double[] result = new double[ values.length ];
		for ( int i = 0; i < values.length; i++ )
		{
			int start = Math.max( 0, i - windowSize + 1 );
			double sum = 0;
			for ( int j = start; j <= i; j++ )
				sum += values[ j ];
			result[ i ] = sum / ( i - start + 1 );
		}
		return result;
	}
}
