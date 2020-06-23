package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import java.awt.*;

public interface Nozzle {
    Amount<Area> throatArea();

    Amount<Force> thrust(Amount<Pressure> Po, Amount<Pressure> Pe, Amount<Pressure> Patm, final double k);

    Shape nozzleShape(Amount<Length> chamberDiameter);
}
