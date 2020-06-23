package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import java.util.List;

public interface Grain {


    java.awt.geom.Area getCrossSection(Amount<Length> regression);

    java.awt.geom.Area getSideView(Amount<Length> regression);

    Amount<Area> surfaceArea(Amount<Length> regression);

    Amount<Volume> volume(Amount<Length> regression);

    Amount<Length> webThickness();

    interface Composite {
        List<Grain> getGrains();
    }

}
