package com.billkuker.rocketry.motorsim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

public class RocketScience {
    private static final Logger log = LogManager.getLogger(RocketScience.class);
    private static final HashSet<WeakReference<UnitPreferenceListener>> prefListeners = new HashSet<>();
    public static Unit<Pressure> PSI = new ProductUnit<>(NonSI.POUND_FORCE.divide(NonSI.INCH.pow(2)));
    public static Unit<Impulse> NEWTON_SECOND = new ProductUnit<>(SI.NEWTON.times(SI.SECOND));
    public static Unit<Impulse> POUND_SECOND = new ProductUnit<>(NonSI.POUND_FORCE.times(SI.SECOND));

    static {
        UnitFormat.getInstance().label(PSI, "psi");
        UnitFormat.getInstance().label(NEWTON_SECOND, "Ns");
    }

    public static void addUnitPreferenceListener(UnitPreferenceListener l) {
        prefListeners.add(new WeakReference<>(l));
    }

    public static <T extends Quantity> String ammountToString(Amount<T> a) {
        if (a == null)
            return "Null";
        final NumberFormat nf = new DecimalFormat("##########.###");
        return nf.format(a.doubleValue(a.getUnit())) + " " + a.getUnit();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Quantity> String ammountToRoundedString(Amount<T> a) {
        if (a == null)
            return "Null";
        Unit<T> u = RocketScience.UnitPreference.preference.getPreferredUnit(a
                .getUnit());
        double d = a.doubleValue(u);

        DecimalFormat df;

        if (u == SI.MILLIMETER && d > 1000.0) {
            u = (Unit<T>) SI.METER;
            d = d / 1000.0;
        } else if (u == NonSI.INCH && d > 12.0) {
            u = (Unit<T>) NonSI.FOOT;
            d = d / 12.0;
        }

        if (Math.abs(d) < 10.0) {
            df = new DecimalFormat("#.##");
        } else if (Math.abs(d) < 100.0) {
            df = new DecimalFormat("#.#");
        } else {
            df = new DecimalFormat("#");
        }

        return df.format(d) + " " + u.toString();
    }

    public enum UnitPreference {
        SI(new Unit[]{
                javax.measure.unit.SI.METERS_PER_SECOND,
                javax.measure.unit.SI.MILLIMETER.pow(3),
                javax.measure.unit.SI.MILLIMETER.pow(2),
                javax.measure.unit.SI.MILLIMETER,
                javax.measure.unit.SI.MILLIMETER.divide(javax.measure.unit.SI.SECOND),
                javax.measure.unit.SI.NEWTON,
                javax.measure.unit.SI.GRAM,
                javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL),
                NEWTON_SECOND
        }),
        NONSI(new Unit[]{
                javax.measure.unit.NonSI.MILES_PER_HOUR,
                javax.measure.unit.NonSI.INCH.pow(3),
                javax.measure.unit.NonSI.INCH.pow(2),
                javax.measure.unit.NonSI.INCH,
                javax.measure.unit.NonSI.POUND_FORCE,
                javax.measure.unit.NonSI.OUNCE,
                javax.measure.unit.NonSI.INCH.divide(javax.measure.unit.SI.SECOND),
                PSI,
                POUND_SECOND
        });

        private static UnitPreference preference;

        static {
            Preferences prefs = Preferences.userNodeForPackage(RocketScience.class);
            String p = prefs.get("PreferedUnits", "SI");
            preference = UnitPreference.valueOf(p);
        }

        protected Set<Unit<?>> units = new HashSet<>();

        UnitPreference(Unit<?>[] u) {
            Collections.addAll(units, u);
        }

        public static UnitPreference getUnitPreference() {
            return preference;
        }

        public static void setUnitPreference(final UnitPreference up) {
            if (preference == up)
                return;
            preference = up;
            Preferences prefs = Preferences.userNodeForPackage(RocketScience.class);
            prefs.put("PreferedUnits", up.toString());
            Iterator<WeakReference<UnitPreferenceListener>> weakIter = prefListeners.iterator();
            while (weakIter.hasNext()) {
                WeakReference<UnitPreferenceListener> weak = weakIter.next();
                UnitPreferenceListener l = weak.get();
                if (l != null) {
                    l.preferredUnitsChanged();
                } else {
                    log.debug("Weak reference to UPE is null");
                    weakIter.remove();
                }
            }
        }

        @SuppressWarnings("unchecked")
        public <T extends Quantity> Unit<T> getPreferredUnit(Unit<T> u) {
            if (units.contains(u))
                return u;
            for (Unit<?> ret : units) {
                if (ret.isCompatible(u)) {
                    return (Unit<T>) ret;
                }
            }
            return u;
        }

        @SuppressWarnings("unchecked")
        public <T extends Quantity> Unit<T> getPreferredUnit(Class<T> q) {
            for (Unit<?> u : units) {
                try {
                    return u.asType(q);
                } catch (ClassCastException e) {
                    //Not compatible
                }
            }
            try {
                Field f = q.getDeclaredField("UNIT");
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Unit.class.isAssignableFrom(f.getType())) {
                        return (Unit<T>) f.get(null);
                    }
                }
            } catch (SecurityException | NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e);
            }
            return null;
        }
    }

    public interface MolarWeight extends Quantity {
        Unit<MolarWeight> UNIT = new ProductUnit<>(
                SI.KILOGRAM.divide(SI.MOLE));
    }

    public interface Impulse extends Quantity {
        Unit<Impulse> UNIT = NEWTON_SECOND;
    }

    public interface UnitPreferenceListener {
        void preferredUnitsChanged();
    }

}
