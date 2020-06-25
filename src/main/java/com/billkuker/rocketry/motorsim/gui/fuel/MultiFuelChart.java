package com.billkuker.rocketry.motorsim.gui.fuel;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.RocketScience;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;

public class MultiFuelChart extends JPanel implements FuelResolver.FuelsChangeListener {
    private static final long serialVersionUID = 1L;

    private final XYSeriesCollection dataset = new XYSeriesCollection();

    private final HashMap<Fuel, XYSeries> fuelToSeries = new HashMap<>();
    private final HashSet<Fuel> editFuels = new HashSet<>();
    private Unit<Pressure> pressureUnit;
    private Unit<Velocity> rateUnit;

    public MultiFuelChart() {
        this.setLayout(new BorderLayout());
        RocketScience.addUnitPreferenceListener(() -> SwingUtilities.invokeLater(() -> {
            setup();
            revalidate();
        }));
        setup();
        FuelResolver.addFuelsChangeListener(this);
    }

    private void setup() {
        pressureUnit = RocketScience.UnitPreference.getUnitPreference()
                .getPreferredUnit(SI.PASCAL);
        rateUnit = RocketScience.UnitPreference.getUnitPreference()
                .getPreferredUnit(SI.METERS_PER_SECOND);
        removeAll();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "", // Title
                pressureUnit.toString(), // x-axis Label
                rateUnit.toString(), // y-axis Label
                dataset, PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tool tips
                false // Configure chart to generate URLs?
        );
        add(new ChartPanel(chart));
        dataset.removeAllSeries();
        fuelToSeries.clear();
        fuelsChanged();
    }

    void addFuel(final Fuel f, final boolean keep) {
        if (keep)
            editFuels.add(f);
        XYSeries s = createSeries(f);
        fuelToSeries.put(f, s);
        dataset.addSeries(s);
        f.addPropertyChangeListener((PropertyChangeListener) evt -> {
            removeFuel(f);
            addFuel(f, keep);
        });
    }

    private XYSeries createSeries(Fuel f) {
        double max = 11;
        double steps = 50;
        XYSeries s = new XYSeries(f.getName());
        for (double dp = 0; dp <= max; dp += max / steps) {
            Amount<Pressure> p = Amount.valueOf(dp, SI.MEGA(SI.PASCAL));
            Amount<Velocity> r = f.burnRate(p);
            s.add(p.doubleValue(pressureUnit), r.doubleValue(rateUnit));
        }
        return s;
    }

    public void removeFuel(Fuel f) {
        XYSeries s = fuelToSeries.get(f);
        if (s == null)
            return;
        dataset.removeSeries(s);
    }

    @Override
    public void fuelsChanged() {
        for (Fuel f : FuelResolver.getFuelMap().values()) {
            if (!fuelToSeries.containsKey(f)) {
                addFuel(f, false);
            }
        }
        for (Fuel f : fuelToSeries.keySet()) {
            if (!FuelResolver.getFuelMap().containsValue(f) && !editFuels.contains(f)) {
                removeFuel(f);
            }
        }
    }
}
