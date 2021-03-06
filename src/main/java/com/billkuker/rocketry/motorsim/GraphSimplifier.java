package com.billkuker.rocketry.motorsim;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Quantity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GraphSimplifier<X extends Quantity, Y extends Quantity> {
    private static final int CHOOSE = 10;
    private static final int EVEN = 10;
    private final SortedMap<Amount<X>, Amount<Y>> out = new TreeMap<>();
    Method f;

    public GraphSimplifier(Object source, String method,
                           Iterator<Amount<X>> domain) throws NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        f = source.getClass().getMethod(method, Amount.class);

        Vector<Entry> oldEntries = new Vector<>();
        Entry max = null;
        while (domain.hasNext()) {
            Amount<X> x = domain.next();
            @SuppressWarnings("unchecked")
            Amount<Y> y = (Amount<Y>) f.invoke(source, x);
            Entry e = new Entry();
            e.x = x;
            e.y = y;
            oldEntries.add(e);
            if (max == null || e.y.isGreaterThan(max.y))
                max = e;
        }

        List<DDEntry> byDD = new Vector<>();
        Map<Amount<X>, Amount<Y>> byX = new HashMap<>();

        for (int i = 1; i < oldEntries.size() - 1; i++) {
            Entry low = oldEntries.elementAt(i - 1);
            Entry middle = oldEntries.elementAt(i);
            Entry high = oldEntries.elementAt(i + 1);

            //if this is one of the N even stepped
            //samples include it.
            if (i % (oldEntries.size() / EVEN) == 0) {
                out.put(middle.x, middle.y);
            }

            Amount<?> d1, d2, dd;

            d1 = middle.y.minus(low.y).divide(middle.x.minus(low.x));
            d2 = high.y.minus(middle.y).divide(high.x.minus(middle.x));

            //dd = (d1.isGreaterThan(d2))?d1:d2;


            dd = d2.minus(d1).divide(high.x.minus(low.x));

            DDEntry dde = new DDEntry();
            dde.dd = dd.abs();
            dde.x = middle.x;

            byDD.add(dde);
            byX.put(middle.x, middle.y);

        }

        Collections.sort(byDD);

        //always include the first, MAX and last
        out.put(oldEntries.elementAt(0).x, oldEntries.elementAt(0).y);
        assert max != null;
        out.put(max.x, max.y);
        int last = oldEntries.size() - 1;
        out.put(oldEntries.elementAt(last).x, oldEntries.elementAt(last).y);

        int count = 0;
        for (DDEntry dde : byDD) {
            if (out.containsKey(dde.x))
                continue;
            out.put(dde.x, byX.get(dde.x));
            if (++count >= CHOOSE)
                break;
        }

    }

    public Amount<Y> value(Amount<X> x) {
        return out.get(x);
    }

    public Iterable<Amount<X>> getDomain() {
        return out.keySet();
    }

    private class Entry {
        Amount<X> x;
        Amount<Y> y;
    }

    @SuppressWarnings("rawtypes")
    private class DDEntry implements Comparable<DDEntry> {
        Amount<X> x;
        Amount dd;

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(DDEntry o) {
            return o.dd.compareTo(dd);
        }
    }

}
