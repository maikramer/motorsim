package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

public class Finocyl extends ExtrudedShapeGrain implements Validating {
    private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);
    private Amount<Length> iD = Amount.valueOf(10, SI.MILLIMETER);
    private Amount<Length> finWidth = Amount.valueOf(2, SI.MILLIMETER);
    private Amount<Length> finDiameter = Amount.valueOf(20, SI.MILLIMETER);
    private int finCount = 5;

    public Finocyl() {
        try {
            setLength(Amount.valueOf(70, SI.MILLIMETER));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        generateGeometry();
    }

    private void generateGeometry() {
        double odmm = oD.doubleValue(SI.MILLIMETER);
        double idmm = iD.doubleValue(SI.MILLIMETER);
        double fwmm = finWidth.doubleValue(SI.MILLIMETER);
        double fdmm = finDiameter.doubleValue(SI.MILLIMETER);

        xsection = new BurningShape();
        Shape outside = new Ellipse2D.Double(-odmm / 2, -odmm / 2, odmm, odmm);
        xsection.add(outside);
        xsection.inhibit(outside);
        xsection.subtract(new Ellipse2D.Double(-idmm / 2, -idmm / 2, idmm, idmm));
        webThickness = null;

        for (int i = 0; i < finCount; i++) {
            Shape fin = new Rectangle2D.Double(-fwmm / 2, 0, fwmm, fdmm / 2);
            xsection.subtract(fin, AffineTransform.getRotateInstance(i * (2.0 * Math.PI / finCount)));
        }
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

    public Amount<Length> getFinWidth() {
        return finWidth;
    }

    public void setFinWidth(Amount<Length> finWidth) throws PropertyVetoException {
        if (finWidth.equals(this.finWidth))
            return;
        this.finWidth = finWidth;
        generateGeometry();
    }


    public Amount<Length> getFinDiameter() {
        return finDiameter;
    }

    public void setFinDiameter(Amount<Length> finDiameter) throws PropertyVetoException {
        if (finDiameter.equals(this.finDiameter))
            return;
        this.finDiameter = finDiameter;
        generateGeometry();
    }


    public int getFinCount() {
        return finCount;
    }

    public void setFinCount(int finCount) throws PropertyVetoException {
        if (finCount == this.finCount)
            return;
        this.finCount = finCount;
        generateGeometry();
    }

    @Override
    public void validate() throws ValidationException {
        if (iD.equals(Amount.ZERO))
            throw new ValidationException("Invalid iD");
        if (oD.equals(Amount.ZERO))
            throw new ValidationException("Invalid oD");
        if (getLength().equals(Amount.ZERO))
            throw new ValidationException("Invalid Length");
        if (iD.isGreaterThan(oD))
            throw new ValidationException("iD > oD");
    }
}
