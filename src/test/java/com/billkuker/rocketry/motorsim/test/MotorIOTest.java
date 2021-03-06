package com.billkuker.rocketry.motorsim.test;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import com.billkuker.rocketry.motorsim.ConvergentDivergentNozzle;
import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.billkuker.rocketry.motorsim.io.MotorIO;

public class MotorIOTest {

	@Test
	public void testReadWrite() throws Exception {
		Motor m = new Motor();
		m.setName("IOTestMotor");
		m.setFuel(FuelResolver.getFuel(new URI("motorsim:KNSB")));

		CylindricalChamber c = new CylindricalChamber();
		c.setLength(Amount.valueOf(200, SI.MILLIMETER));
		c.setInnerDiameter(Amount.valueOf(30, SI.MILLIMETER));
		m.setChamber(c);

		CoredCylindricalGrain g = new CoredCylindricalGrain();
		try {
			g.setLength(Amount.valueOf(70, SI.MILLIMETER));
			g.setOuterDiameter(Amount.valueOf(29, SI.MILLIMETER));
			g.setInnerDiameter(Amount.valueOf(8, SI.MILLIMETER));
		} catch (PropertyVetoException v) {
			throw new Error(v);
		}

		m.setGrain(new MultiGrain(g, 2));

		ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
		n.setThroatDiameter(Amount.valueOf(7.9, SI.MILLIMETER));
		n.setExitDiameter(Amount.valueOf(9, SI.MILLIMETER));
		n.setEfficiency(.87);
		m.setNozzle(n);

		File f = new File("MotorIOTest.xml");

		MotorIO.writeMotor(m, new FileOutputStream(f));

		MotorIO.readMotor(new FileInputStream(f));

	}
}
