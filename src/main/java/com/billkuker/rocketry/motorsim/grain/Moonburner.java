package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyVetoException;

public class Moonburner extends ExtrudedShapeGrain implements Validating {

    private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
    private Amount<Length> iD = Amount.valueOf(10, SI.MILLIMETER);
    private Amount<Length> coreOffset = Amount.valueOf(0, SI.MILLIMETER);

    public Moonburner() {
        try {
            setLength(Amount.valueOf(70, SI.MILLIMETER));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        generateGeometry();
    }

    public Amount<Length> getOD() {
        return oD;
    }

    public void setOD(Amount<Length> od) throws PropertyVetoException {
        if (od.equals(this.oD))
            return;
        this.oD = od;
        generateGeometry();
    }

    public Amount<Length> getID() {
        return iD;
    }

    public void setID(Amount<Length> id) throws PropertyVetoException {
        if (id.equals(this.iD))
            return;
        iD = id;
        generateGeometry();
    }

    public Amount<Length> getCoreOffset() {
        return coreOffset;
    }

    public void setCoreOffset(Amount<Length> coreOffset)
            throws PropertyVetoException {
        if (coreOffset.equals(this.coreOffset))
            return;
        this.coreOffset = coreOffset;
        generateGeometry();
    }

    private void generateGeometry() {
        double odmm = oD.doubleValue(SI.MILLIMETER);
        double idmm = iD.doubleValue(SI.MILLIMETER);
        double offmm = coreOffset.doubleValue(SI.MILLIMETER);
        xsection = new BurningShape();
        Shape outside = new Ellipse2D.Double(0, 0, odmm, odmm);
        xsection.add(outside);
        xsection.inhibit(outside);

        xsection.subtract(new Ellipse2D.Double(odmm / 2 - idmm / 2 + offmm, odmm / 2 - idmm / 2, idmm, idmm));
        webThickness = null;
    }

    public void validate() throws ValidationException {
        if (iD.equals(Amount.ZERO))
            throw new ValidationException("Invalid iD");
        if (oD.equals(Amount.ZERO))
            throw new ValidationException("Invalid oD");
        if (getLength().equals(Amount.ZERO))
            throw new ValidationException("Invalid Length");
        if (iD.isGreaterThan(oD))
            throw new ValidationException("iD > oD");
        if (coreOffset.isGreaterThan(iD.plus(oD).divide(2.0)))
            throw new ValidationException("Core offset too large");
    }

}
