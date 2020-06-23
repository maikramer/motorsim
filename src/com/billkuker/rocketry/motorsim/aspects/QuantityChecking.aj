package com.billkuker.rocketry.motorsim.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import javax.measure.unit.Unit;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.Fuel;

public aspect QuantityChecking {

	private static final Logger log = LogManager.getLogger(QuantityChecking.class);
	public interface Checked {
	};

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel extends Checked;

	@SuppressWarnings({"rawtypes","unchecked"})
	void around(Checked c, Amount amt):
	        execution(void Checked+.set*(Amount)) && target(c) && args(amt) {
		try {
			BeanInfo b = Introspector.getBeanInfo(c.getClass());
			PropertyDescriptor[] ps = b.getPropertyDescriptors();
			String name = thisJoinPointStaticPart.getSignature().getName();
			name = name.replaceFirst("set", "");
			for (PropertyDescriptor propertyDescriptor : ps) {
				if (propertyDescriptor.getName().equals(name)) {
					Type t = propertyDescriptor.getReadMethod().getGenericReturnType();
					ParameterizedType p = (ParameterizedType) t;
					Class expected = (Class) p.getActualTypeArguments()[0];
					Field f = expected.getDeclaredField("UNIT");
					Unit u = (Unit) f.get(null);

					if (!amt.getUnit().isCompatible(u)) {
						log.warn("Aspect Expected " + expected
								+ " got " + u);

						throw new Error(propertyDescriptor.getShortDescription()
								+ " must be in units of "
								+ expected.getSimpleName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		proceed(c, amt);
	}
}
