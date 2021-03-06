package com.billkuker.rocketry.motorsim.aspects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Grain;
import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Burn.BurnSettings;

public aspect ChangeListening {

	private static final Logger log = LogManager.getLogger(Subject.class);
	public interface Subject {
		// public void addPropertyChangeListener(PropertyChangeListener l);
	};

	private PropertyChangeSupport Subject.pcs;

	public void Subject.addPropertyChangeListener(PropertyChangeListener l) {
		if(pcs == null) pcs = new PropertyChangeSupport(Subject.class);
		pcs.addPropertyChangeListener(l);
	}
	
	public void Subject.removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}
	
	public void Subject.firePropertyChange(PropertyChangeEvent e) {
		pcs.firePropertyChange(e);
	}

	declare parents: Motor || Grain || Chamber || Nozzle || Fuel || BurnSettings implements Subject;

	void around(Subject s, Object newVal):
	        execution(void Subject+.set*(..)) && target(s) && args(newVal) {
		String name = thisJoinPointStaticPart.getSignature().getName();
		name = name.replaceFirst("set", "");
		Object old = null;
		try {
			String pre = "get";
			if (newVal instanceof Boolean)
				pre = "is";
			Method m = s.getClass().getMethod(pre + name);
			old = m.invoke(s);

		} catch (Throwable t) {
			log.warn("Error getting old value for " + name);
		}
		proceed(s, newVal);
		if (old != newVal) {
			s.pcs.firePropertyChange(name, old, newVal);
		}
	}

	after(Subject s) returning: this(s) && initialization(Subject.new(..)) {
		s.pcs = new PropertyChangeSupport(s);
	}
}
