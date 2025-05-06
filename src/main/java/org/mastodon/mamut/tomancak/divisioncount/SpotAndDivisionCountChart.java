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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.Triple;
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
import org.mastodon.mamut.ProjectModel;
import org.scijava.prefs.PrefService;

public class SpotAndDivisionCountChart extends JFrame
{

	private static final String SPOT_COLOR = "spotColor";

	private static final String DIVISION_COLOR = "divisionColor";

	private static final String SPOT_COUNT_VISIBILITY = "spotCountVisibility";

	private static final String DIVISION_COUNT_VISIBILITY = "divisionCountVisibility";

	private static final String SPOT_COUNT_AVERAGE_VISIBILITY = "spotCountAverageVisibility";

	private static final String DIVISION_COUNT_AVERAGE_VISIBILITY = "divisionCountAverageVisibility";

	private static final String SPOT_COUNT_SLIDING_AVERAGE_WINDOW_SIZE = "spotCountSlidingAverageWindowSize";

	private static final String DIVISION_COUNT_SLIDING_AVERAGE_WINDOW_SIZE = "divisionCountSlidingAverageWindowSize";

	private static final int SPOT_COUNT_DEFAULT_COLOR = new Color( 230, 159, 0 ).getRGB(); // Dark Orange

	private static final int DIVISION_COUNT_DEFAULT_COLOR = new Color( 86, 180, 233 ).getRGB(); // Light Blue

	private static final int DEFAULT_SLIDING_WINDOW_SIZE = 10;

	private Color spotCountColor; // Dark Orange

	private Color divisionCountColor; // Light Blue

	private final static String TITLE = "Spot and Division Counts over Time";
	private final static String SPOTS_COUNT_SERIES_NAME = "Spot Counts";
	private final static String DIVISION_COUNT_SERIES_NAME = "Division Counts";

	private final XYPlot plot;

	private final NumberAxis leftAxis;

	private final NumberAxis rightAxis;

	private int spotWindowSize;

	private int divisionWindowSize;

	private final PrefService prefs;

	SpotAndDivisionCountChart( final double[] timepoints, final double[] spotCounts, final double[] divisionCounts,
			final PrefService prefs )
	{
		this.prefs = prefs;

		this.spotCountColor = new Color(
				prefs.getInt( SpotAndDivisionCountChart.class, SPOT_COLOR, SPOT_COUNT_DEFAULT_COLOR ) );
		this.divisionCountColor = new Color(
				prefs.getInt( SpotAndDivisionCountChart.class, DIVISION_COLOR, DIVISION_COUNT_DEFAULT_COLOR ) );
		this.spotWindowSize =
				prefs.getInt( SpotAndDivisionCountChart.class, SPOT_COUNT_SLIDING_AVERAGE_WINDOW_SIZE, DEFAULT_SLIDING_WINDOW_SIZE );
		this.divisionWindowSize =
				prefs.getInt( SpotAndDivisionCountChart.class, DIVISION_COUNT_SLIDING_AVERAGE_WINDOW_SIZE, DEFAULT_SLIDING_WINDOW_SIZE );

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

		repaint();
	}

	public static void show( final ProjectModel projectModel, final PrefService prefService )
	{
		List< Triple< Integer, Integer, Integer > > divisionCounts =
				SpotAndDivisionCount.getSpotAndDivisionsPerTimepoint( projectModel.getModel() );
		double[] timepoints = divisionCounts.stream().mapToDouble( Triple::getLeft ).toArray();
		double[] spots = divisionCounts.stream().mapToDouble( Triple::getMiddle ).toArray();
		double[] divisions = divisionCounts.stream().mapToDouble( Triple::getRight ).toArray();

		SpotAndDivisionCountChart chart = new SpotAndDivisionCountChart( timepoints, spots, divisions, prefService );
		chart.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		chart.setVisible( true );
	}

	/**
	 * Creates a control panel with color choosers and checkboxes for visibility controls.
	 */
	private JPanel createControlPanel( final double[] timepoints, final double[] spotCounts, final double[] divisionCounts )
	{
		JPanel controlPanel = new JPanel( new MigLayout( "fill, wrap 5", "[grow]", "[]10[]10[]10[]10[]" ) );

		// Spot-related controls
		JButton spotColorButton = new JButton( "Choose Spot Color" );
		spotColorButton.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog( null, "Choose Spot Color", spotCountColor );
			if ( newColor != null )
			{
				spotCountColor = newColor;
				prefs.put( SpotAndDivisionCountChart.class, SPOT_COLOR, spotCountColor.getRGB() );
				updateChartColors();
			}
		} );

		boolean spotCountVisibility = prefs.getBoolean( SpotAndDivisionCountChart.class, SPOT_COUNT_VISIBILITY, true );
		boolean spotCountAverageVisibility = prefs.getBoolean( SpotAndDivisionCountChart.class, SPOT_COUNT_AVERAGE_VISIBILITY, true );
		JCheckBox showSpotCounts = new JCheckBox( "Show Spot Counts", spotCountVisibility );
		JCheckBox showSpotAverage = new JCheckBox( "Show Spot Sliding Average", spotCountAverageVisibility );

		showSpotCounts.addActionListener( e -> {
			plot.getRenderer( 0 ).setSeriesVisible( 0, showSpotCounts.isSelected() );
			prefs.put( SpotAndDivisionCountChart.class, SPOT_COUNT_VISIBILITY, showSpotCounts.isSelected() );
			updateAxisVisibility();
		} );

		showSpotAverage.addActionListener( e -> {
			plot.getRenderer( 0 ).setSeriesVisible( 1, showSpotAverage.isSelected() );
			prefs.put( SpotAndDivisionCountChart.class, SPOT_COUNT_AVERAGE_VISIBILITY, showSpotAverage.isSelected() );
			updateAxisVisibility();
		} );

		JSpinner spotWindowSpinner = new JSpinner( new SpinnerNumberModel( spotWindowSize, 1, Integer.MAX_VALUE, 1 ) );
		spotWindowSpinner.addChangeListener( e -> {
			spotWindowSize = ( int ) spotWindowSpinner.getValue();
			prefs.put( SpotAndDivisionCountChart.class, SPOT_COUNT_SLIDING_AVERAGE_WINDOW_SIZE, spotWindowSize );
			updateSlidingAverage( timepoints, spotCounts, divisionCounts );
		} );

		// Division-related controls
		JButton divisionColorButton = new JButton( "Choose Division Color" );
		divisionColorButton.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog( null, "Choose Division Color", divisionCountColor );
			if ( newColor != null )
			{
				divisionCountColor = newColor;
				prefs.put( SpotAndDivisionCountChart.class, DIVISION_COLOR, divisionCountColor.getRGB() );
				updateChartColors();
			}
		} );

		boolean divisionCountVisibility = prefs.getBoolean( SpotAndDivisionCountChart.class, DIVISION_COUNT_VISIBILITY, true );
		boolean divisionAverageVisibility = prefs.getBoolean( SpotAndDivisionCountChart.class, DIVISION_COUNT_AVERAGE_VISIBILITY, true );
		JCheckBox showDivisionCounts = new JCheckBox( "Show Division Counts", divisionCountVisibility );
		JCheckBox showDivisionAverage = new JCheckBox( "Show Division Sliding Average", divisionAverageVisibility );

		showDivisionCounts.addActionListener( e -> {
			plot.getRenderer( 1 ).setSeriesVisible( 0, showDivisionCounts.isSelected() );
			prefs.put( SpotAndDivisionCountChart.class, DIVISION_COUNT_VISIBILITY, showDivisionCounts.isSelected() );
			updateAxisVisibility();
		} );

		showDivisionAverage.addActionListener( e -> {
			plot.getRenderer( 1 ).setSeriesVisible( 1, showDivisionAverage.isSelected() );
			prefs.put( SpotAndDivisionCountChart.class, DIVISION_COUNT_AVERAGE_VISIBILITY, showDivisionAverage.isSelected() );
			updateAxisVisibility();
		} );

		JSpinner divisionWindowSpinner = new JSpinner( new SpinnerNumberModel( divisionWindowSize, 1, Integer.MAX_VALUE, 1 ) );
		divisionWindowSpinner.addChangeListener( e -> {
			divisionWindowSize = ( int ) divisionWindowSpinner.getValue();
			prefs.put( SpotAndDivisionCountChart.class, DIVISION_COUNT_SLIDING_AVERAGE_WINDOW_SIZE, divisionWindowSize );
			updateSlidingAverage( timepoints, spotCounts, divisionCounts );
		} );

		// Add components to the control panel
		controlPanel.add( spotColorButton, "growx" );
		controlPanel.add( showSpotCounts, "growx" );
		controlPanel.add( showSpotAverage, "growx" );
		controlPanel.add( new JLabel( "Window Size:" ), "align right" );
		controlPanel.add( spotWindowSpinner, "wmax 50" );

		controlPanel.add( divisionColorButton, "growx" );
		controlPanel.add( showDivisionCounts, "growx" );
		controlPanel.add( showDivisionAverage, "growx" );
		controlPanel.add( new JLabel( "Window Size:" ), "align right" );
		controlPanel.add( divisionWindowSpinner, "wmax 50" );

		// Add description
		controlPanel.add( new JLabel(
				"<html>This windows shows the number of spots and divisions at each timepoint together with a sliding average.<br>A division is defined as a spot with more than one outgoing edge.</html>" ),
				"span 5" );

		plot.getRenderer( 0 ).setSeriesVisible( 0, spotCountVisibility );
		plot.getRenderer( 0 ).setSeriesVisible( 1, spotCountAverageVisibility );
		plot.getRenderer( 1 ).setSeriesVisible( 0, divisionCountVisibility );
		plot.getRenderer( 1 ).setSeriesVisible( 1, divisionAverageVisibility );

		updateAxisVisibility();

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
