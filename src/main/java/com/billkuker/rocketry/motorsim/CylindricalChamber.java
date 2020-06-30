package com.billkuker.rocketry.motorsim;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class CylindricalChamber implements Chamber, ICylindricalChamber {
    private Amount<Length> length = Amount.valueOf(200, SI.MILLIMETER);
    @XStreamAlias("OD")
    private Amount<Length> outerDiameter = Amount.valueOf(31, SI.MILLIMETER);
    @XStreamAlias("ID")
    private Amount<Length> innerDiameter = Amount.valueOf(30, SI.MILLIMETER);
    public CylindricalChamber() {
    }

    public Amount<Pressure> getBurstPressure() {
        return null;
    }

    public Amount<Volume> chamberVolume() {
        return innerDiameter.divide(2).pow(2).times(Math.PI).times(length).to(SI.CUBIC_METRE);
    }

    public Amount<Length> getLength() {
        return length;
    }

    public void setLength(Amount<Length> length) {
        this.length = length;
    }

    public Amount<Length> getInnerDiameter() {
        return innerDiameter;
    }

    public void setInnerDiameter(Amount<Length> id) {
        innerDiameter = id;
    }

    public Amount<Length> getOuterDiameter() {
        return outerDiameter;
    }

    public void setOuterDiameter(Amount<Length> oD) {
        this.outerDiameter = oD;
    }

    @Override
    public Shape chamberShape() {
        double ir = innerDiameter.doubleValue(SI.MILLIMETER) / 2;
        double or = outerDiameter.doubleValue(SI.MILLIMETER) / 2;
        double lenmm = length.doubleValue(SI.MILLIMETER);
        double thick = or - ir;

        Rectangle2D.Double l, r, t;
        l = new Rectangle2D.Double(-or, 0, thick, lenmm);
        r = new Rectangle2D.Double(ir, 0, thick, lenmm);
        t = new Rectangle2D.Double(-or, 0, or * 2, thick);
        Area a = new Area(l);
        a.add(new Area(r));
        a.add(new Area(t));
        return a;
    }

}
