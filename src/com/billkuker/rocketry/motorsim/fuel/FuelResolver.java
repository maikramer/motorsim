package com.billkuker.rocketry.motorsim.fuel;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class FuelResolver {
    private static final Logger log = LogManager.getLogger(FuelResolver.class);
    private static final Set<WeakReference<FuelsChangeListener>> listeners = new HashSet<>();
    private static final Map<URI, Fuel> fuels = new HashMap<URI, Fuel>();
    private static final Map<Fuel, URI> uris = new HashMap<Fuel, URI>();

    static {
        try {
            add(new KNSB(), new URI("motorsim:KNSB"));
            add(new KNDX(), new URI("motorsim:KNDX"));
            add(new KNSU(), new URI("motorsim:KNSU"));
            add(new KNER(), new URI("motorsim:KNER"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void addFuelsChangeListener(FuelsChangeListener l) {
        listeners.add(new WeakReference<>(l));
    }

    public static void removeFuelsChangeListener(FuelsChangeListener l) {
        listeners.remove(l);
    }

    public static Map<URI, Fuel> getFuelMap() {
        return Collections.unmodifiableMap(fuels);
    }

    public static URI getURI(Fuel f) {
        return uris.get(f);
    }

    public static Fuel getFuel(URI u) throws FuelNotFound {
        if (fuels.containsKey(u))
            return fuels.get(u);
        return tryResolve(u);
    }

    private static Fuel tryResolve(URI u) throws FuelNotFound {
        File f = new File(u);
        try {
            Fuel fuel = MotorIO.readFuel(new FileInputStream(f));
            add(fuel, u);
            return fuel;
        } catch (IOException e) {
            throw new FuelNotFound();
        }
    }

    public static void add(Fuel f, URI uri) {
        fuels.put(uri, f);
        uris.put(f, uri);

        Iterator<WeakReference<FuelsChangeListener>> weakIter = listeners.iterator();
        while (weakIter.hasNext()) {
            WeakReference<FuelsChangeListener> weak = weakIter.next();
            FuelsChangeListener l = weak.get();
            if (l != null) {
                l.fuelsChanged();
            } else {
                log.debug("Weak reference to FCL is null");
                weakIter.remove();
            }
        }
    }

    public interface FuelsChangeListener {
        void fuelsChanged();
    }

    public static class FuelNotFound extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
