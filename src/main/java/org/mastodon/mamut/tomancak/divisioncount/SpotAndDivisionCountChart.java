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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class SpotAndDivisionCountChart extends JFrame
{
	private Color divisionCountColor = new Color( 86, 180, 233 ); // Light Blue

	private Color spotCountColor = new Color( 230, 159, 0 ); // Dark Orange

	private final static String TITLE = "Spot and Division Counts over Time";
	private final static String SPOTS_COUNT_SERIES_NAME = "Spot Counts";
	private final static String DIVISION_COUNT_SERIES_NAME = "Division Counts";

	private final XYPlot plot;

	private final NumberAxis leftAxis;

	private final NumberAxis rightAxis;

	private int spotWindowSize;

	private int divisionWindowSize;

	SpotAndDivisionCountChart( double[] timepoints, double[] spotCounts, double[] divisionCounts, int windowSize )
	{
		this.spotWindowSize = windowSize;
		this.divisionWindowSize = windowSize;

		XYSeriesCollection spotCountsSeries = createSeries(
				timepoints, spotCounts, SPOTS_COUNT_SERIES_NAME, spotWindowSize );

		XYSeriesCollection divisionCountsSeries = createSeries(
				timepoints, divisionCounts, DIVISION_COUNT_SERIES_NAME, divisionWindowSize );

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

		plot = chart.getXYPlot();

		// Customize the left y-axis (Spot Counts)
		leftAxis = ( NumberAxis ) plot.getRangeAxis();
		customizeAxis( leftAxis, SPOTS_COUNT_SERIES_NAME, spotCountColor );
		plot.setRangeAxis( 0, leftAxis );
		plot.setDataset( 0, spotCountsSeries );
		plot.mapDatasetToRangeAxis( 0, 0 );

		// Add the right y-axis (Division Counts)
		rightAxis = new NumberAxis();
		customizeAxis( rightAxis, DIVISION_COUNT_SERIES_NAME, divisionCountColor );
		plot.setRangeAxis( 1, rightAxis );
		plot.setDataset( 1, divisionCountsSeries );
		plot.mapDatasetToRangeAxis( 1, 1 );

		XYLineAndShapeRenderer spotCountRenderer = createRenderer(
				spotCountColor, new Rectangle2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 0, spotCountRenderer );
		XYLineAndShapeRenderer divisionCountRenderer = createRenderer(
				divisionCountColor, new Ellipse2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 1, divisionCountRenderer );

		// Set up the chart panel
		ChartPanel chartPanel = new ChartPanel( chart );
		chartPanel.setPreferredSize( new Dimension( 800, 600 ) );

		// Add color chooser and visibility controls
		JPanel controlPanel = createControlPanel( timepoints, spotCounts, divisionCounts );

		// Set up the frame layout
		setLayout( new BorderLayout() );
		add( chartPanel, BorderLayout.CENTER );
		add( controlPanel, BorderLayout.SOUTH );

		// Set up the frame
		setTitle( TITLE );
		setSize( 800, 700 );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setLocationRelativeTo( null );
	}

	/**
	 * Creates a control panel with color choosers and checkboxes for visibility controls.
	 */
	private JPanel createControlPanel( final double[] timepoints, final double[] spotCounts, final double[] divisionCounts )
	{
		JPanel controlPanel = new JPanel( new MigLayout( "wrap 5" ) );

		// Spot-related controls
		JButton spotColorButton = new JButton( "Choose Spot Color" );
		spotColorButton.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog( null, "Choose Spot Color", spotCountColor );
			if ( newColor != null )
			{
				spotCountColor = newColor;
				updateChartColors();
			}
		} );

		JCheckBox showSpotCounts = new JCheckBox( "Show Spot Counts", true );
		JCheckBox showSpotAverage = new JCheckBox( "Show Spot Sliding Average", true );

		showSpotCounts.addActionListener( e -> {
			plot.getRenderer( 0 ).setSeriesVisible( 0, showSpotCounts.isSelected() );
			updateAxisVisibility();
		} );

		showSpotAverage.addActionListener( e -> {
			plot.getRenderer( 0 ).setSeriesVisible( 1, showSpotAverage.isSelected() );
			updateAxisVisibility();
		} );

		JSpinner spotWindowSpinner = new JSpinner( new SpinnerNumberModel( spotWindowSize, 1, Integer.MAX_VALUE, 1 ) );
		spotWindowSpinner.addChangeListener( e -> {
			spotWindowSize = ( int ) spotWindowSpinner.getValue();
			updateSlidingAverage( timepoints, spotCounts, divisionCounts );
		} );

		// Division-related controls
		JButton divisionColorButton = new JButton( "Choose Division Color" );
		divisionColorButton.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog( null, "Choose Division Color", divisionCountColor );
			if ( newColor != null )
			{
				divisionCountColor = newColor;
				updateChartColors();
			}
		} );

		JCheckBox showDivisionCounts = new JCheckBox( "Show Division Counts", true );
		JCheckBox showDivisionAverage = new JCheckBox( "Show Division Sliding Average", true );

		showDivisionCounts.addActionListener( e -> {
			plot.getRenderer( 1 ).setSeriesVisible( 0, showDivisionCounts.isSelected() );
			updateAxisVisibility();
		} );

		showDivisionAverage.addActionListener( e -> {
			plot.getRenderer( 1 ).setSeriesVisible( 1, showDivisionAverage.isSelected() );
			updateAxisVisibility();
		} );

		JSpinner divisionWindowSpinner = new JSpinner( new SpinnerNumberModel( divisionWindowSize, 1, Integer.MAX_VALUE, 1 ) );
		divisionWindowSpinner.addChangeListener( e -> {
			divisionWindowSize = ( int ) divisionWindowSpinner.getValue();
			updateSlidingAverage( timepoints, spotCounts, divisionCounts );
		} );

		// Add components to the control panel
		controlPanel.add( spotColorButton, "growx" );
		controlPanel.add( showSpotCounts, "growx" );
		controlPanel.add( showSpotAverage, "growx" );
		controlPanel.add( new JLabel( "Window Size:" ), "align right" );
		controlPanel.add( spotWindowSpinner, "growx" );

		controlPanel.add( divisionColorButton, "growx" );
		controlPanel.add( showDivisionCounts, "growx" );
		controlPanel.add( showDivisionAverage, "growx" );
		controlPanel.add( new JLabel( "Window Size:" ), "align right" );
		controlPanel.add( divisionWindowSpinner, "growx" );

		return controlPanel;
	}

	/**
	 * Updates the chart colors based on the user's selection.
	 */
	private void updateChartColors()
	{
		// Update the renderer colors
		XYLineAndShapeRenderer spotCountRenderer = createRenderer(
				spotCountColor, new Rectangle2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 0, spotCountRenderer );

		XYLineAndShapeRenderer divisionCountRenderer = createRenderer(
				divisionCountColor, new Ellipse2D.Double( -2, -2, 4, 4 ) );
		plot.setRenderer( 1, divisionCountRenderer );

		// Update axis label colors
		customizeAxis( leftAxis, SPOTS_COUNT_SERIES_NAME, spotCountColor );
		customizeAxis( rightAxis, DIVISION_COUNT_SERIES_NAME, divisionCountColor );

		// Repaint the chart
		repaint();
	}

	/**
	 * Updates the visibility of the axis labels based on the state of the checkboxes.
	 */
	private void updateAxisVisibility()
	{
		XYItemRenderer spotCountRenderer = plot.getRenderer( 0 );
		Boolean spotCountVisible = spotCountRenderer.getSeriesVisible( 0 );
		Boolean spotAverageVisible = spotCountRenderer.getSeriesVisible( 1 );
		boolean showSpotAxis = spotCountVisible != null && spotCountVisible || spotAverageVisible != null && spotAverageVisible;
		leftAxis.setLabel( showSpotAxis ? SPOTS_COUNT_SERIES_NAME : null );
		leftAxis.setVisible( showSpotAxis );

		XYItemRenderer divisionCountRenderer = plot.getRenderer( 1 );
		Boolean divisionCountVisible = divisionCountRenderer.getSeriesVisible( 0 );
		Boolean divisionAverageVisible = divisionCountRenderer.getSeriesVisible( 1 );
		boolean showDivisionAxis =
				divisionCountVisible != null && divisionCountVisible || divisionAverageVisible != null && divisionAverageVisible;
		rightAxis.setLabel( showDivisionAxis ? DIVISION_COUNT_SERIES_NAME : null );
		rightAxis.setVisible( showDivisionAxis );

		repaint();
	}

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

	/**
	 * Updates the sliding average series based on the new window sizes.
	 */
	private void updateSlidingAverage( double[] timepoints, double[] spotCounts, double[] divisionCounts )
	{
		XYSeriesCollection spotCountsSeries = createSeries(
				timepoints, spotCounts, SPOTS_COUNT_SERIES_NAME, spotWindowSize );
		XYSeriesCollection divisionCountsSeries = createSeries(
				timepoints, divisionCounts, DIVISION_COUNT_SERIES_NAME, divisionWindowSize );

		plot.setDataset( 0, spotCountsSeries );
		plot.setDataset( 1, divisionCountsSeries );
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
