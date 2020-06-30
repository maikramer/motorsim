package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Grain;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import java.beans.PropertyVetoException;

public abstract class ExtrudedGrain implements Grain {
    private final Amount<Length> endLight = Amount.valueOf(0, SI.MILLIMETER);
    @XStreamAlias("foreEndInhibited")
    private boolean upperEndInhibited = false;
    @XStreamAlias("aftEndInhibited")
    private boolean lowerEndInhibited = false;
    private Amount<Length> length = Amount.valueOf(100, SI.MILLIMETER);

    protected int numberOfBurningEnds(Amount<Length> regression) {
        if (regression.isLessThan(endLight))
            return 0;
        return (upperEndInhibited ? 0 : 1) + (lowerEndInhibited ? 0 : 1);
    }

    protected Amount<Length> regressedLength(Amount<Length> regression) {
        if (regression.isLessThan(endLight))
            return length;
        return length.minus(regression.minus(endLight).times(numberOfBurningEnds(regression)));
    }

    public boolean isUpperEndInhibited() {
        return upperEndInhibited;
    }

    public void setUpperEndInhibited(boolean upperEndInhibited) throws PropertyVetoException {
        this.upperEndInhibited = upperEndInhibited;
    }

    public boolean isLowerEndInhibited() {
        return lowerEndInhibited;
    }

    public void setLowerEndInhibited(boolean lowerEndInhibited) throws PropertyVetoException {
        this.lowerEndInhibited = lowerEndInhibited;
    }

    public Amount<Length> getLength() {
        return length;
    }

    public void setLength(Amount<Length> length) throws PropertyVetoException {
        this.length = length;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
