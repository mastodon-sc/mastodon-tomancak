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
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

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
	private final static Color DIVISION_COUNT_COLOR = new Color( 86, 180, 233 ); // Light Blue

	private final static Color SPOT_COUNT_COLOR = new Color( 230, 159, 0 ); // Dark Orange

	private final static String TITLE = "Spot and Division Counts over Time";

	private final static String SPOTS_COUNT_SERIES_NAME = "Spot Counts";

	private final static String DIVISION_COUNT_SERIES_NAME = "Division Counts";

	DivisionCountChart( double[] timepoints, double[] spotCounts, double[] divisionCounts, int windowSize )
	{
		XYSeriesCollection spotCountsSeries = createSeries(
				timepoints, spotCounts, SPOTS_COUNT_SERIES_NAME, windowSize );

		XYSeriesCollection divisionCountsSeries = createSeries(
				timepoints, divisionCounts, DIVISION_COUNT_SERIES_NAME, windowSize );

		JFreeChart chart = ChartFactory.createXYLineChart(
				TITLE,
				"Timepoints",
				null,
				spotCountsSeries,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
		);

		XYPlot plot = chart.getXYPlot();

		// Customize the left y-axis (Spot Counts)
		NumberAxis leftAxis = ( NumberAxis ) plot.getRangeAxis();
		customizeAxis( leftAxis, SPOTS_COUNT_SERIES_NAME, SPOT_COUNT_COLOR );
		plot.setRangeAxis( 0, leftAxis );
		plot.setDataset( 0, spotCountsSeries );
		plot.mapDatasetToRangeAxis( 0, 0 );

		// Add the right y-axis (Division Counts)
		NumberAxis rightAxis = new NumberAxis();
		customizeAxis( rightAxis, DIVISION_COUNT_SERIES_NAME, DIVISION_COUNT_COLOR );
		plot.setRangeAxis( 1, rightAxis );
		plot.setDataset( 1, divisionCountsSeries );
		plot.mapDatasetToRangeAxis( 1, 1 );

		XYLineAndShapeRenderer spotCountRenderer = createRenderer(
				SPOT_COUNT_COLOR, new Rectangle2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 0, spotCountRenderer );
		XYLineAndShapeRenderer divisionCountRenderer = createRenderer(
				DIVISION_COUNT_COLOR, new Ellipse2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 1, divisionCountRenderer );

		// Set up the chart panel
		ChartPanel chartPanel = new ChartPanel( chart );
		chartPanel.setPreferredSize( new Dimension( 800, 600 ) );
		setContentPane( chartPanel );
		setChartColors( plot );

		// Set up the frame
		setTitle( TITLE );
		setSize( 800, 600 );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setLocationRelativeTo( null );
	}

	/**
	 * Creates a reusable renderer for datasets.
	 *
	 * @param color  The color for the raw data points and the line.
	 * @param shape     The shape for the raw data points.
	 * @return A configured XYLineAndShapeRenderer.
	 */
	private XYLineAndShapeRenderer createRenderer( final Color color, final Shape shape )
	{
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		// Configure raw data points
		renderer.setSeriesPaint( 0, color );
		renderer.setSeriesShape( 0, shape );
		renderer.setSeriesShapesVisible( 0, true );
		renderer.setSeriesLinesVisible( 0, false );

		// Configure sliding average line
		renderer.setSeriesPaint( 1, color );
		renderer.setSeriesShapesVisible( 1, false );
		renderer.setSeriesLinesVisible( 1, true );
		renderer.setSeriesStroke( 1, new BasicStroke( 2.0f ) );

		return renderer;
	}

	/**
	 * Customizes an axis with bold font and a specific color.
	 *
	 * @param axis  The axis to customize.
	 * @param label The label for the axis.
	 * @param color The color for the axis font.
	 */
	private void customizeAxis( final NumberAxis axis, final String label, final Color color )
	{
		axis.setLabel( label );
		axis.setLabelFont( new Font( Font.DIALOG, Font.BOLD, 14 ) );
		axis.setLabelPaint( color );
		axis.setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 12 ) );
		axis.setTickLabelPaint( color );
	}

	private static XYSeriesCollection createSeries( final double[] xValues, final double[] yValues, final String seriesName,
			final int windowSize )
	{
		XYSeries originalSeries = new XYSeries( seriesName );
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

	private static void setChartColors( final XYPlot plot )
	{
		plot.setBackgroundPaint( Color.WHITE );
		plot.setDomainGridlinePaint( Color.BLACK );
		plot.setRangeGridlinePaint( Color.BLACK );
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
