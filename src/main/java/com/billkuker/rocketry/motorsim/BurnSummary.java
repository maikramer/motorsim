package com.billkuker.rocketry.motorsim;

import com.billkuker.rocketry.motorsim.Burn.Interval;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.*;
import javax.measure.unit.SI;

public class BurnSummary {
    Amount<RocketScience.Impulse> ns = Amount.valueOf(0,
            RocketScience.NEWTON_SECOND);

    Amount<Duration> thrustTime = Amount.valueOf(0, SI.SECOND);
    Amount<Force> maxThrust = Amount.valueOf(0, SI.NEWTON);
    Amount<Pressure> maxPressure = Amount.valueOf(0, SI.MEGA(SI.PASCAL));
    Amount<Duration> isp;
    Amount<Mass> propellantMass;
    Double saftyFactor;
    Double volumeLoading;

    public BurnSummary(Burn b) {
        for (Interval i : b.getData().values()) {
            ns = ns.plus(i.dt.times(i.thrust));
            if (i.thrust.isGreaterThan(Amount.valueOf(0.01, SI.NEWTON))) {
                thrustTime = thrustTime.plus(i.dt);
            }
            if (i.thrust.isGreaterThan(maxThrust))
                maxThrust = i.thrust;
            if (i.chamberPressure.isGreaterThan(maxPressure))
                maxPressure = i.chamberPressure;
        }

        isp = ns
                .divide(b
                        .getMotor()
                        .getGrain()
                        .volume(Amount.valueOf(0, SI.MILLIMETER))
                        .times(b.getMotor()
                                .getFuel()
                                .getIdealDensity()
                                .times(b.getMotor().getFuel()
                                        .getDensityRatio())))
                .to(SI.METERS_PER_SECOND)
                .divide(Amount.valueOf(9.81,
                        SI.METERS_PER_SQUARE_SECOND)).to(SI.SECOND);

        if (b.getMotor().getChamber().getBurstPressure() != null)
            saftyFactor = b.getMotor().getChamber().getBurstPressure().divide(maxPressure).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);

        Amount<Volume> vol = b.getMotor().getGrain().volume(Amount.valueOf(0, SI.MILLIMETER));
        Amount<VolumetricDensity> ideal = b.getMotor().getFuel().getIdealDensity();
        Amount<VolumetricDensity> actual = ideal.times(b.getMotor().getFuel().getDensityRatio());
        propellantMass = vol.times(actual).to(SI.GRAM);

        Amount<Volume> chamber = b.getMotor().getChamber().chamberVolume();
        volumeLoading = vol.divide(chamber).to(Dimensionless.UNIT).doubleValue(Dimensionless.UNIT);
    }

    public String getRating() {
        float cnf = (float) (Math.log(ns
                .doubleValue(RocketScience.NEWTON_SECOND) / 1.25) / Math
                .log(2));
        int cn = (int) cnf;
        float fraction = cnf - cn;
        int percent = (int) (100 * fraction);
        char cl = (char) ((int) 'A' + cn);

        return percent + "% "
                + new String(new char[]{cl}) + "-"
                + Math.round(averageThrust().doubleValue(SI.NEWTON));
    }

    public Double getSaftyFactor() {
        return saftyFactor;
    }

    public Amount<RocketScience.Impulse> totalImpulse() {
        return ns;
    }

    public Amount<Duration> thrustTime() {
        return thrustTime;
    }

    public Amount<Force> maxThrust() {
        return maxThrust;
    }

    public Amount<Duration> specificImpulse() {
        return isp;
    }

    public Amount<Force> averageThrust() {
        Amount<Force> averageThrust = Amount.valueOf(0, SI.NEWTON);
        if (thrustTime().isGreaterThan(Amount.valueOf(0, SI.SECOND)))
            averageThrust = totalImpulse().divide(thrustTime).to(SI.NEWTON);
        return averageThrust;
    }

    public Amount<Pressure> maxPressure() {
        return maxPressure;
    }

    public Amount<Mass> getPropellantMass() {
        return propellantMass;
    }

    public double getVolumeLoading() {
        return volumeLoading;
    }

    @Override
    public String toString() {

        return "====== Burn Summary ======\nRating: " +
                getRating() +
                "\nTotal Impulse: " +
                RocketScience.ammountToRoundedString(totalImpulse()) +
                "\nISP: " +
                RocketScience.ammountToRoundedString(specificImpulse()) +
                "\nThrust: " +
                "\n\tMax: " +
                RocketScience.ammountToRoundedString(maxThrust()) +
                "\n\tAvg: " +
                RocketScience.ammountToRoundedString(averageThrust()) +
                "\nMax Pressure: " +
                RocketScience.ammountToRoundedString(maxPressure()) +
                "\nBurn Time: " +
                RocketScience.ammountToRoundedString(thrustTime()) +
                "\n=======================";
    }

}
