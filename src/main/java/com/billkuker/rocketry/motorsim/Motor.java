package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

public class Motor implements Validating {
    private Chamber chamber;
    private Grain grain;
    private Nozzle nozzle;
    private Fuel fuel;
    private String name;
    private String manufacturer = "MF";
    private Amount<Mass> casingWeight = Amount.valueOf(0, SI.KILOGRAM);
    private Amount<Duration> ejectionDelay = Amount.valueOf(5, SI.SECOND);

    public Motor() {
    }

    public void validate() throws ValidationException {
        if (chamber.chamberVolume().isLessThan(grain.volume(Amount.valueOf(0, SI.MILLIMETER)))) {
            throw new ValidationException("Fuel does not fit in chamber");
        }
        if (chamber instanceof Validating)
            ((Validating) chamber).validate();
        if (grain instanceof Validating)
            ((Validating) grain).validate();
        if (nozzle instanceof Validating)
            ((Validating) nozzle).validate();
        if (fuel instanceof Validating)
            ((Validating) fuel).validate();
    }

    public Chamber getChamber() {
        return chamber;
    }

    public void setChamber(Chamber chamber) {
        this.chamber = chamber;
    }

    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain grain) {
        this.grain = grain;
    }

    public Nozzle getNozzle() {
        return nozzle;
    }

    public void setNozzle(Nozzle nozzle) {
        this.nozzle = nozzle;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Amount<Duration> getEjectionDelay() {
        return ejectionDelay;
    }

    public void setEjectionDelay(Amount<Duration> ejectionDelay) {
        this.ejectionDelay = ejectionDelay;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Amount<Mass> getCasingWeight() {
        return casingWeight;
    }

    public void setCasingWeight(Amount<Mass> casingWeight) {
        this.casingWeight = casingWeight;
    }
}
