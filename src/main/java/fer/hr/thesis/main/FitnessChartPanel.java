package fer.hr.thesis.main;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Chart which shows the results training gives on training set and validation
 * set through training.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class FitnessChartPanel extends JPanel {

	/**
	 * Number used for serialization.
	 */
	private static final long serialVersionUID = 404054803206686450L;
	/**
	 * Function representing validation results of the chart.
	 */
	private XYSeries validation;
	/**
	 * Function representing training results of the chart.
	 */
	private XYSeries training;
	/**
	 * Collection of all the functions inside the chart.
	 */
	private XYSeriesCollection dataset;

	/**
	 * Reference to the chart displayed inside the panel.
	 */
	private JFreeChart chart;

	/**
	 * Creates a new FitnessChartPanel.
	 */
	public FitnessChartPanel() {
		super();
		this.validation = new XYSeries("Validation set");
		this.training = new XYSeries("Training set");
		dataset = new XYSeriesCollection();
		dataset.addSeries(validation);
		dataset.addSeries(training);
		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);

	}

	/**
	 * Creates the chart and sets its axes.
	 * @param dataset Functions of the graph
	 * @return Created chart
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createXYLineChart("Training", "Epochs", "Free energy", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		final XYPlot plot = result.getXYPlot();
		result.setBackgroundPaint(getBackground());

		ValueAxis axis = plot.getDomainAxis();
		axis.setRange(0, 1000);
		axis.setAutoRange(true);
		axis = plot.getRangeAxis();
		axis.setRange(-10000, 0);
		axis.setAutoRange(true);

		return result;
	}

	/**
	 * Number of the current iteration.
	 */
	private int currIteration = 0;

	/**
	 * Adds a new value to the current chart.
	 * @param trainingResult Result on training set
	 * @param validationResult Result on validation set
	 * @param interval Number of epochs since the last result
	 */
	public void addValue(double trainingResult, double validationResult, int interval) {
		this.training.add(currIteration, trainingResult);
		this.validation.add(currIteration, validationResult);
		currIteration += interval;
	}

	/**
	 * Clears the data of the previous graph from the panel.
	 */
	public void clearGraph() {
		currIteration = 0;
		this.training.clear();
		this.validation.clear();
	}
}