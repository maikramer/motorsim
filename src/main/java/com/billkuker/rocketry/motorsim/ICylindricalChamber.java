package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;

public interface ICylindricalChamber {
    Amount<Length> getLength();


    Amount<Length> getInnerDiameter();


    Amount<Length> getOuterDiameter();
}
