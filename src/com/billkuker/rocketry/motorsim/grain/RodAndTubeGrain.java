package com.billkuker.rocketry.motorsim.grain;

import java.beans.PropertyVetoException;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.billkuker.rocketry.motorsim.Grain;

public class RodAndTubeGrain extends CompoundGrain {
	CoredCylindricalGrain rod, tube;
	
	public RodAndTubeGrain() {
		try{
			rod = new CoredCylindricalGrain();
			rod.setInnerSurfaceInhibited(true);
			rod.setID(Amount.valueOf(0, SI.MILLIMETER));
			rod.setOD(Amount.valueOf(10, SI.MILLIMETER));
			rod.setOuterSurfaceInhibited(false);
			rod.setEndSurfaceInhibited(true);
			
			tube = new CoredCylindricalGrain();
			tube.setInnerSurfaceInhibited(false);
			tube.setID(Amount.valueOf(20, SI.MILLIMETER));
			tube.setOD(Amount.valueOf(30, SI.MILLIMETER));
			tube.setOuterSurfaceInhibited(true);
			tube.setEndSurfaceInhibited(true);
		} catch ( PropertyVetoException v ){
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

	public boolean isEndSurfaceInhibited() {
		return rod.isEndSurfaceInhibited();
	}

	public void setEndSurfaceInhibited(boolean endSurfaceInhibited)
			throws PropertyVetoException {
		rod.setEndSurfaceInhibited(endSurfaceInhibited);
		tube.setEndSurfaceInhibited(endSurfaceInhibited);
	}

	public Amount<Length> getLength() {
		return rod.getLength();
	}

	public void setLength(Amount<Length> length) throws PropertyVetoException {
		rod.setLength(length);
		tube.setLength(length);
	}

}
