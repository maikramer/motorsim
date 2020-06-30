package com.billkuker.rocketry.motorsim.grain;

import com.billkuker.rocketry.motorsim.Validating;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import java.beans.PropertyVetoException;

public class RodAndTubeGrain extends CompoundGrain implements Validating {
    public static RodAndTubeGrain DEFAULT_GRAIN = new RodAndTubeGrain() {
        {
            try {
                setOd(Amount.valueOf(30, SI.MILLIMETER));
                setTubeID(Amount.valueOf(20, SI.MILLIMETER));
                setRodDiameter(Amount.valueOf(10, SI.MILLIMETER));
                setForeEndInhibited(true);
                setAftEndInhibited(true);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    };
    CoredCylindricalGrain rod, tube;

    public RodAndTubeGrain() {
        try {
            rod = new CoredCylindricalGrain();
            rod.setInnerDiameter(Amount.valueOf(0, SI.MILLIMETER));
            rod.setInnerSurfaceInhibited(true);
            rod.setOuterSurfaceInhibited(false);
            tube = new CoredCylindricalGrain();
            tube.setInnerSurfaceInhibited(false);

            setOd(Amount.valueOf(30, SI.MILLIMETER));
            setTubeID(Amount.valueOf(20, SI.MILLIMETER));
            setRodDiameter(Amount.valueOf(10, SI.MILLIMETER));
            setForeEndInhibited(true);
            setAftEndInhibited(true);
        } catch (PropertyVetoException v) {
            v.printStackTrace();
            //I know these values are OK
        }
        add(rod);
        add(tube);
    }

    public Amount<Length> getRodDiameter() {
        return rod.getOuterDiameter();
    }

    public void setRodDiameter(Amount<Length> od) throws PropertyVetoException {
        rod.setOuterDiameter(od);
    }

    public Amount<Length> getTubeID() {
        return tube.getInnerDiameter();
    }

    public void setTubeID(Amount<Length> id) throws PropertyVetoException {
        tube.setInnerDiameter(id);
    }

    public Amount<Length> getOd() {
        return tube.getOuterDiameter();
    }

    public void setOd(Amount<Length> od) throws PropertyVetoException {
        tube.setOuterDiameter(od);
    }

    public Amount<Length> getLength() {
        return rod.getLength();
    }

    public void setLength(Amount<Length> length) throws PropertyVetoException {
        rod.setLength(length);
        tube.setLength(length);
    }

    public boolean isAftEndInhibited() {
        return rod.isLowerEndInhibited();
    }

    public void setAftEndInhibited(boolean aftEndInhibited)
            throws PropertyVetoException {
        rod.setLowerEndInhibited(aftEndInhibited);
        tube.setLowerEndInhibited(aftEndInhibited);
    }

    public boolean isForeEndInhibited() {
        return rod.isUpperEndInhibited();
    }

    public void setForeEndInhibited(boolean foreEndInhibited)
            throws PropertyVetoException {
        rod.setUpperEndInhibited(foreEndInhibited);
        tube.setUpperEndInhibited(foreEndInhibited);
    }

    @Override
    public void validate() throws ValidationException {
        rod.validate();
        tube.validate();
        if (rod.getOuterDiameter().isGreaterThan(tube.getInnerDiameter()))
            throw new ValidationException("Rod does not fit inside tube");

    }
}
