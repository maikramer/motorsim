package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.grain.util.BurningShape;
import com.billkuker.rocketry.motorsim.grain.util.ExtrudedShapeGrain;
import com.billkuker.rocketry.motorsim.gui.visual.Editor;
import com.billkuker.rocketry.motorsim.gui.visual.GrainPanel;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyVetoException;

public class MultiPort extends ExtrudedShapeGrain implements Validating {

    private boolean inhibitOutside = true;

    private Amount<Length> oD = Amount.valueOf(30, SI.MILLIMETER);

    private Amount<Length> coreD = Amount.valueOf(3, SI.MILLIMETER);

    private Amount<Length> r1D = Amount.valueOf(2, SI.MILLIMETER);
    private Amount<Length> r1Offset = Amount.valueOf(5, SI.MILLIMETER);
    private int r1Count = 4;

    private Amount<Length> r2D = Amount.valueOf(1, SI.MILLIMETER);
    private Amount<Length> r2Offset = Amount.valueOf(10, SI.MILLIMETER);
    private int r2Count = 8;

    private Amount<Angle> ringTwoRot = Amount.valueOf(22.5, NonSI.DEGREE_ANGLE);

    public MultiPort() {
        try {
            setLength(Amount.valueOf(70, SI.MILLIMETER));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        generateGeometry();
    }

    public static void main(String[] args) {
        Grain g;
        new GrainPanel(g = new MultiPort()).showAsWindow();
        new Editor(g).showAsWindow();
    }

    @Override
    public void validate() throws ValidationException {
        // TODO Auto-generated method stub

    }

    private void generateGeometry() {
        double odmm = oD.doubleValue(SI.MILLIMETER);
        double cdmm = coreD.doubleValue(SI.MILLIMETER);
        double r1dmm = r1D.doubleValue(SI.MILLIMETER);
        double r1off = r1Offset.doubleValue(SI.MILLIMETER);

        double r2dmm = r2D.doubleValue(SI.MILLIMETER);
        double r2off = r2Offset.doubleValue(SI.MILLIMETER);

        double r2rot = ringTwoRot.doubleValue(SI.RADIAN);

        xsection = new BurningShape();
        Shape outside = new Ellipse2D.Double(-odmm / 2, -odmm / 2, odmm, odmm);
        xsection.add(outside);

        if (isInhibitOutside())
            xsection.inhibit(outside);

        xsection.subtract(new Ellipse2D.Double(-cdmm / 2, -cdmm / 2, cdmm, cdmm));
        webThickness = null;

        for (int i = 0; i < r1Count; i++) {
            Shape port = new Ellipse2D.Double(r1off - r1dmm / 2, -r1dmm / 2,
                    r1dmm, r1dmm);
            xsection.subtract(
                    port,
                    AffineTransform.getRotateInstance(i
                            * (2.0 * Math.PI / r1Count)));
        }

        for (int i = 0; i < r2Count; i++) {
            Shape port = new Ellipse2D.Double(r2off - r2dmm / 2, -r2dmm / 2,
                    r2dmm, r2dmm);
            xsection.subtract(
                    port,
                    AffineTransform.getRotateInstance(r2rot + i
                            * (2.0 * Math.PI / r2Count)));
        }
    }

    public Amount<Length> getoD() {
        return oD;
    }

    public void setoD(Amount<Length> oD) {
        this.oD = oD;
        generateGeometry();
    }

    public Amount<Length> getCoreD() {
        return coreD;
    }

    public void setCoreD(Amount<Length> coreD) {
        this.coreD = coreD;
        generateGeometry();
    }

    public Amount<Length> getR1D() {
        return r1D;
    }

    public void setR1D(Amount<Length> r1d) {
        r1D = r1d;
        generateGeometry();
    }

    public Amount<Length> getR1Offset() {
        return r1Offset;
    }

    public void setR1Offset(Amount<Length> r1Offset) {
        this.r1Offset = r1Offset;
        generateGeometry();
    }

    public int getR1Count() {
        return r1Count;
    }

    public void setR1Count(int r1Count) {
        this.r1Count = r1Count;
        generateGeometry();
    }

    public Amount<Length> getR2D() {
        return r2D;
    }

    public void setR2D(Amount<Length> r2d) {
        r2D = r2d;
        generateGeometry();
    }

    public Amount<Length> getR2Offset() {
        return r2Offset;
    }

    public void setR2Offset(Amount<Length> r2Offset) {
        this.r2Offset = r2Offset;
        generateGeometry();
    }

    public int getR2Count() {
        return r2Count;
    }

    public void setR2Count(int r2Count) {
        this.r2Count = r2Count;
        generateGeometry();
    }

    public Amount<Angle> getRingTwoRot() {
        return ringTwoRot;
    }

    public void setRingTwoRot(Amount<Angle> ringTwoRot) {
        this.ringTwoRot = ringTwoRot;
        generateGeometry();
    }

    public boolean isInhibitOutside() {
        return inhibitOutside;
    }

    public void setInhibitOutside(boolean inhibitOutside) {
        this.inhibitOutside = inhibitOutside;
        generateGeometry();
    }
}
