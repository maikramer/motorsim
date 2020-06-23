package com.billkuker.rocketry.motorsim.fuel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class PiecewiseSaintRobertFuel extends SaintRobertFuel {

    private static final Logger log = LogManager.getLogger(PiecewiseSaintRobertFuel.class);
    private final SortedMap<Amount<Pressure>, Entry> entries = new TreeMap<Amount<Pressure>, Entry>();

    protected PiecewiseSaintRobertFuel(Type t) {
        super(t);
    }

    protected void add(Amount<Pressure> p, final double _a, final double _n) {
        entries.put(p, new Entry() {{
            a = _a;
            n = _n;
        }});
    }

    protected void clear() {
        entries.clear();
    }

    @Override
    protected double burnrateCoefficient(Amount<Pressure> pressure) {
        try {
            Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
            Entry e = entries.get(samplePressure);
            return e.a;
        } catch (NoSuchElementException e) {
            log.warn("Pressure " + pressure + " is outside of expiermental range for " + this.getClass().getSimpleName());
            try {
                return entries.get(entries.lastKey()).a;
            } catch (NoSuchElementException ee) {
                log.error("No data to return!");
                return 0;
            }
        }
    }

    @Override
    protected double burnrateExponent(Amount<Pressure> pressure) {
        try {
            Amount<Pressure> samplePressure = entries.tailMap(pressure).firstKey();
            Entry e = entries.get(samplePressure);
            return e.n;
        } catch (NoSuchElementException e) {
            log.warn("Pressure " + pressure + " is outside of expiermental range for " + this.getClass().getSimpleName());
            try {
                return entries.get(entries.lastKey()).n;
            } catch (NoSuchElementException ee) {
                log.error("No data to return!");
                return 0;
            }
        }
    }

    private static class Entry {
        double a;
        double n;
    }

}
