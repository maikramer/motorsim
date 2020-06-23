package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Volume;
import java.awt.*;

public interface Chamber {
    Amount<Volume> chamberVolume();

    Amount<Pressure> getBurstPressure();

    Shape chamberShape();
}
