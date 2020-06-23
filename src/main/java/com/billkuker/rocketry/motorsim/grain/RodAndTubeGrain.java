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
            rod.setID(Amount.valueOf(0, SI.MILLIMETER));
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
        return rod.getOD();
    }

    public void setRodDiameter(Amount<Length> od) throws PropertyVetoException {
        rod.setOD(od);
    }

    public Amount<Length> getTubeID() {
        return tube.getID();
    }

    public void setTubeID(Amount<Length> id) throws PropertyVetoException {
        tube.setID(id);
    }

    public Amount<Length> getOd() {
        return tube.getOD();
    }

    public void setOd(Amount<Length> od) throws PropertyVetoException {
        tube.setOD(od);
    }

    public Amount<Length> getLength() {
        return rod.getLength();
    }

    public void setLength(Amount<Length> length) throws PropertyVetoException {
        rod.setLength(length);
        tube.setLength(length);
    }

    public boolean isAftEndInhibited() {
        return rod.isAftEndInhibited();
    }

    public void setAftEndInhibited(boolean aftEndInhibited)
            throws PropertyVetoException {
        rod.setAftEndInhibited(aftEndInhibited);
        tube.setAftEndInhibited(aftEndInhibited);
    }

    public boolean isForeEndInhibited() {
        return rod.isForeEndInhibited();
    }

    public void setForeEndInhibited(boolean foreEndInhibited)
            throws PropertyVetoException {
        rod.setForeEndInhibited(foreEndInhibited);
        tube.setForeEndInhibited(foreEndInhibited);
    }

    @Override
    public void validate() throws ValidationException {
        rod.validate();
        tube.validate();
        if (rod.getOD().isGreaterThan(tube.getID()))
            throw new ValidationException(this, "Rod does not fit inside tube");

    }
}
