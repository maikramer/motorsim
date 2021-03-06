package com.billkuker.rocketry.motorsim.motors.nakka;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.KNSB;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.gui.visual.BurnPanel;
import org.jscience.physics.amount.Amount;

import javax.measure.unit.NonSI;
import java.beans.PropertyVetoException;

public class KappaSB extends Motor {
    public KappaSB() {
        setName("Kappa-Sorbitol");
        setFuel(new KNSB());

        CylindricalChamber c = new CylindricalChamber();
        c.setLength(Amount.valueOf(16, NonSI.INCH));
        c.setInnerDiameter(Amount.valueOf(2.37, NonSI.INCH));
        setChamber(c);

        CoredCylindricalGrain g = new CoredCylindricalGrain();
        try {
            g.setLength(Amount.valueOf(3.8, NonSI.INCH));
            g.setOuterDiameter(Amount.valueOf(2.23, NonSI.INCH));
            g.setInnerDiameter(Amount.valueOf(.75, NonSI.INCH));
        } catch (PropertyVetoException v) {
            throw new Error(v);
        }

        setGrain(new MultiGrain(g, 4));

        ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
        n.setThroatDiameter(Amount.valueOf(.502, NonSI.INCH));
        n.setExitDiameter(Amount.valueOf(1.67, NonSI.INCH));
        n.setEfficiency(.87);
        setNozzle(n);
    }

    public static void main(String[] args) {
        KappaSB m = new KappaSB();
        Burn b = new Burn(m);
        new BurnPanel(b).showAsWindow();
    }

}
