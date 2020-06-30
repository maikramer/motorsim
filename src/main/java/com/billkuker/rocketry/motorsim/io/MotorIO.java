package com.billkuker.rocketry.motorsim.io;

import com.billkuker.rocketry.motorsim.CylindricalChamber;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.ICylindricalChamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver.FuelNotFound;
import com.billkuker.rocketry.motorsim.grain.CoredCylindricalGrain;
import com.billkuker.rocketry.motorsim.grain.ExtrudedGrain;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class MotorIO {
    private static final Logger log = LogManager.getLogger(MotorIO.class);

    private static XStream getXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.omitField(Motor.class, "pcs");
        xstream.omitField(MultiGrain.class, "pcs");
        xstream.processAnnotations(Motor.class);
        xstream.processAnnotations(MultiGrain.class);
        xstream.processAnnotations(ExtrudedGrain.class);
        xstream.processAnnotations(CoredCylindricalGrain.class);
        xstream.processAnnotations(CylindricalChamber.class);
        xstream.processAnnotations(ICylindricalChamber.class);
        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        xstream.registerConverter(new AmountConverter());
        xstream.registerConverter(new FuelConverter());
        JavaBeanConverter converter = new JavaBeanConverter(xstream.getMapper());
        xstream.registerConverter(converter, XStream.PRIORITY_VERY_LOW);
        return xstream;
    }

    private static XStream getFuelXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        xstream.registerConverter(new AmountConverter());
        JavaBeanConverter converter = new JavaBeanConverter(xstream.getMapper());
        xstream.registerConverter(converter, XStream.PRIORITY_VERY_LOW);

        return xstream;
    }

    public static void writeFuel(Fuel f, OutputStream os) throws IOException {
        ObjectOutputStream out = getFuelXStream().createObjectOutputStream(os);
        out.writeObject(f);
        out.close();
        os.flush();
    }

    public static Fuel readFuel(InputStream is) throws IOException {
        ObjectInputStream in = getFuelXStream().createObjectInputStream(is);
        Fuel f;
        try {
            f = (Fuel) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found", e);
        }
        return f;
    }

    public static void writeMotor(Motor m, OutputStream os) throws IOException {
        ObjectOutputStream out = getXStream().createObjectOutputStream(os);
        out.writeObject(m);
        out.close();
        os.flush();
    }

    public static Motor readMotor(InputStream is) throws IOException {
        ObjectInputStream in = getXStream().createObjectInputStream(is);
        Motor m;
        try {
            m = (Motor) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found", e);
        }
        return m;
    }

    static class FuelConverter implements Converter {

        @Override
        public boolean canConvert(Class c) {
            return Fuel.class.isAssignableFrom(c);
        }

        @Override
        public void marshal(Object o, HierarchicalStreamWriter w,
                            MarshallingContext ctx) {
            Fuel f = (Fuel) o;
            URI uri = FuelResolver.getURI(f);
            ctx.convertAnother(uri);
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader r,
                                UnmarshallingContext ctx) {
            Object o = ctx.currentObject();
            Object uri = null;
            Class<?> c = ctx.getRequiredType();
            try {
                uri = ctx.convertAnother(o, URI.class);
            } catch (ConversionException e) {
                e.printStackTrace();
            }
            if (uri instanceof URI) {
                try {
                    return FuelResolver.getFuel((URI) uri);
                } catch (FuelNotFound e) {
                    throw new ConversionException("Unable to load fuel " + uri, e);
                }
            }

            try {
                return FuelResolver.getFuel(new URI("motorsim:" + c.getSimpleName()));
            } catch (FuelNotFound | URISyntaxException e) {
                log.error(e);
            }
            return null;
        }

    }

    static class AmountConverter implements Converter {

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void marshal(Object o, HierarchicalStreamWriter w, MarshallingContext c) {
            Amount a = (Amount) o;
            String text;
            //Leave off the fractional part if it is not relevant so we get exact values back
            if (a.isExact())
                text = a.getExactValue() + " " + a.getUnit();
            else
                text = a.doubleValue(a.getUnit()) + " " + a.getUnit();
            w.setValue(fix(text));
        }

        public Object unmarshal(HierarchicalStreamReader r, UnmarshallingContext c) {
            String text = r.getValue();
            return Amount.valueOf(unfix(text));
        }

        private String fix(String s) {
            return s.replace("\u00B3", "^3");
        }

        private String unfix(String s) {
            return s.replace("^3", "\u00B3");
        }

        public boolean canConvert(Class c) {
            return c.equals(Amount.class);
        }


    }

}
