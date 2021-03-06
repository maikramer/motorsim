package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.aspects.ChangeListening;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiGrain implements Grain, Grain.Composite, PropertyChangeListener, Validating {

    private final Amount<Length> delay = Amount.valueOf(0, SI.MILLIMETER);
    private Grain grain = null;
    private int count = 1;
    private Amount<Length> spacing = Amount.valueOf(5, SI.MILLIMETER);

    public MultiGrain(Grain g, int c) {
        count = c;
        setGrain(g);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (count <= 0)
            throw new IllegalArgumentException("Must have at least 1 grain");
        this.count = count;
    }

    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain g) {
        if (grain instanceof ChangeListening.Subject) {
            ((ChangeListening.Subject) grain).addPropertyChangeListener(this);
        }
        grain = g;
        if (g instanceof ChangeListening.Subject) {
            ((ChangeListening.Subject) g).addPropertyChangeListener(this);
        }
    }

    public Amount<Length> getSpacing() {
        return spacing;
    }

    public void setSpacing(Amount<Length> spacing) {
        this.spacing = spacing;
    }

    private Amount<Length> getAdjustedRegression(Amount<Length> regression, int grain) {
        double flush = 1;
        return regression.minus(delay.times(grain)).times(Math.pow(flush, grain));
    }

    public Amount<Area> surfaceArea(Amount<Length> regression) {
        Amount<Area> ret = Amount.valueOf(0, SI.SQUARE_METRE);
        for (int i = 0; i < count; i++) {
            ret = ret.plus(grain.surfaceArea(getAdjustedRegression(regression, i)));
        }
        return ret;
    }

    public Amount<Volume> volume(Amount<Length> regression) {
        Amount<Volume> ret = Amount.valueOf(0, SI.CUBIC_METRE);
        for (int i = 0; i < count; i++) {
            ret = ret.plus(grain.volume(getAdjustedRegression(regression, i)));
        }
        return ret;
    }

    public Amount<Length> webThickness() {
        Amount<Length> thickness = grain.webThickness();
        if (thickness != null) return thickness.plus(delay.times(count));
        else return Amount.valueOf(0, SI.MILLIMETER);
    }

    public java.awt.geom.Area getCrossSection(Amount<Length> regression) {
        return grain.getCrossSection(regression);
    }

    public java.awt.geom.Area getSideView(Amount<Length> regression) {
        Rectangle2D unburntBounds = grain.getSideView(Amount.valueOf(0, SI.MILLIMETER)).getBounds2D();

        java.awt.geom.Area ret = new java.awt.geom.Area();

        for (int i = 0; i < count; i++) {
            java.awt.geom.Area g = grain.getSideView(getAdjustedRegression(regression, i));
            ret.add(g);
            ret.transform(AffineTransform.getTranslateInstance(0, -(unburntBounds.getHeight() + spacing.doubleValue(SI.MILLIMETER))));
        }
        return ret;
    }

    public List<Grain> getGrains() {

        ArrayList<Grain> ret = new ArrayList<Grain>();
        ret.add(grain);
        return Collections.unmodifiableList(ret);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }

    @Override
    public void validate() throws ValidationException {
        if (grain instanceof Validating)
            ((Validating) grain).validate();
    }

}
