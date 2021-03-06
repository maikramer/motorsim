package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.RocketScience;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Pressure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MultiMotorPressureChart extends JPanel implements BurnWatcher {
    private static final long serialVersionUID = 1L;

    private final XYSeriesCollection dataset = new XYSeriesCollection();

    private final HashMap<Burn, XYSeries> burnToSeries = new HashMap<>();
    private Unit<Duration> time;
    private Unit<Pressure> pressureUnit;

    public MultiMotorPressureChart() {
        this.setLayout(new BorderLayout());
        RocketScience.addUnitPreferenceListener(() -> SwingUtilities.invokeLater(() -> {
            removeAll();
            dataset.removeAllSeries();
            Set<Burn> burns = new HashSet<>(burnToSeries.keySet());
            burnToSeries.clear();
            setup();
            for (Burn b : burns) {
                addBurn(b);
            }
            revalidate();
        }));
        setup();
    }

    private void setup() {
        time = RocketScience.UnitPreference.getUnitPreference()
                .getPreferredUnit(SI.SECOND);
        pressureUnit = RocketScience.UnitPreference.getUnitPreference()
                .getPreferredUnit(SI.PASCAL);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "", // Title
                "Time (" + time.toString() + ")", // x-axis Label
                "Chamber Pressure (" + pressureUnit.toString() + ")", // y-axis Label
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
        for (Burn.Interval i : b.getData().values()) {
            s.add(i.time.doubleValue(time), i.chamberPressure.doubleValue(pressureUnit));
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
