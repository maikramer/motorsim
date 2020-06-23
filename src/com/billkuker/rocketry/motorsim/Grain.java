package com.billkuker.rocketry.motorsim;

import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;

import org.jscience.physics.amount.Amount;

public interface Grain {
	
	
	java.awt.geom.Area getCrossSection(Amount<Length> regression);
	java.awt.geom.Area getSideView(Amount<Length> regression);
	
	interface Composite {
		List<Grain> getGrains();
	}

	Amount<Area> surfaceArea(Amount<Length> regression);
	
	Amount<Volume> volume(Amount<Length> regression);
	
	Amount<Length> webThickness();

}
