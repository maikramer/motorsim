package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.RocketScience;

public class MultiMotorThrustChart extends JPanel implements BurnWatcher {
	private static final long serialVersionUID = 1L;

	private XYSeriesCollection dataset = new XYSeriesCollection();

	private HashMap<Burn, XYSeries> burnToSeries = new HashMap<Burn, XYSeries>();
	private Unit<Duration> time;
	private Unit<Force> force;

	public MultiMotorThrustChart() {
		this.setLayout(new BorderLayout());
		time = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(SI.SECOND);
		force = RocketScience.UnitPreference.getUnitPreference()
				.getPreferredUnit(SI.NEWTON);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"", // Title
				"Time (" + time.toString() + ")", // x-axis Label
				"Thrust (" + force.toString() + ")", // y-axis Label
				dataset, PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tool tips
				false // Configure chart to generate URLs?
				);
		add(new ChartPanel(chart));
	}

	public void addBurn(Burn b) {
		XYSeries s = createSeries(b);
		burnToSeries.put(b, s);
		dataset.addSeries(s);
	}

	private XYSeries createSeries(Burn b) {
		XYSeries s = new XYSeries(b.getMotor().getName());
		for( Burn.Interval i : b.getData().values() ){
			s.add(i.time.doubleValue(time), i.thrust.doubleValue(force));
		}
		return s;
	}

	public void removeBurn(Burn b) {
		XYSeries s = burnToSeries.get(b);
		if (s == null)
			return;
		dataset.removeSeries(s);
	}

	@Override
	public void replace(Burn oldBurn, Burn newBurn) {
		removeBurn(oldBurn);
		addBurn(newBurn);
	}
}