package com.billkuker.rocketry.motorsim;

import com.billkuker.rocketry.motorsim.RocketScience.MolarWeight;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;

public interface Fuel {

    String getName();

    Amount<VolumetricDensity> getIdealDensity();

    Amount<Velocity> burnRate(Amount<Pressure> pressure);

    CombustionProduct getCombustionProduct();

    double getDensityRatio();

    double getCombustionEfficiency();

    interface CombustionProduct {
        Amount<Temperature> getIdealCombustionTemperature();

        Amount<MolarWeight> getEffectiveMolarWeight();

        double getRatioOfSpecificHeats();

        double getRatioOfSpecificHeats2Phase();
    }

}
