package com.billkuker.rocketry.motorsim.io;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.GraphSimplifier;
import com.billkuker.rocketry.motorsim.ICylindricalChamber;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class ENGExporter {

    public static void export(Iterable<Burn> bb, File f) throws IOException {
        export(bb, new FileOutputStream(f));
    }

    public static void export(Iterable<Burn> bb, OutputStream os) throws IOException {
        for (Burn b : bb) {
            export(b, os);
        }
    }

    public static void export(Burn b, OutputStream os) throws IOException {

        ICylindricalChamber cha = (ICylindricalChamber) b.getMotor().getChamber();

        NumberFormat nf = new DecimalFormat("0.####", new DecimalFormatSymbols(Locale.US));

        StringBuilder out = new StringBuilder();

        out.append(";Output from Motorsim, motorsim@billkuker.com\n");
        out.append(";Name Diameter Length Delays ProWt Wt Manufacturer\n");
        out.append(b.getMotor().getName().replace(" ", "-")).append(" ");

        double dia = cha.getOuterDiameter().doubleValue(SI.MILLIMETER);
        double len = cha.getLength().doubleValue(SI.MILLIMETER);

        Amount<Mass> prop = b.getMotor().getGrain().volume(
                Amount.valueOf(0, SI.MILLIMETER)).times(
                b.getMotor().getFuel().getIdealDensity().times(
                        b.getMotor().getFuel().getDensityRatio())).to(
                SI.KILOGRAM);
        double wt = prop.doubleValue(SI.KILOGRAM);

        double delay = b.getMotor().getEjectionDelay().doubleValue(SI.SECOND);
        String delayString = Integer.toString((int) delay);
        String manufacturer = b.getMotor().getManufacturer();
        double caseWt = b.getMotor().getCasingWeight().doubleValue(SI.KILOGRAM);

        out.append(nf.format(dia)).append(" ").append(nf.format(len)).append(" ").append(delayString).append("-0-0 ").append(nf.format(wt)).append(" ")
                .append(nf.format(wt + caseWt)).append(" ").append(manufacturer).append("\n");

        GraphSimplifier<Duration, Force> gs;
        try {
            gs = new GraphSimplifier<>(b, "thrust", b.getData()
                    .keySet().iterator());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        int cnt = 0;
        double lastTime = 0;
        for (Amount<Duration> t : gs.getDomain()) {
            cnt++;
            double thrust = gs.value(t).doubleValue(SI.NEWTON);
            if (cnt < 10 && thrust == 0.0) {
                continue; // This is a hack to ignore 0 thrust early in burn
            }
            out.append("   ");
            out.append(nf.format(lastTime = t.doubleValue(SI.SECOND)));
            out.append(" ");
            out.append(nf.format(thrust));
            out.append("\n");
        }

        out.append("   ");
        out.append(nf.format(lastTime + 0.01));
        out.append(" ");
        out.append(nf.format(0.0));
        out.append("\n");
        out.append(";\n\n");

        os.write(out.toString().getBytes());
    }
}
