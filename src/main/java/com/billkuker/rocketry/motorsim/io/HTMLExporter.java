package com.billkuker.rocketry.motorsim.io;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.BurnSummary;
import com.billkuker.rocketry.motorsim.GraphSimplifier;
import com.billkuker.rocketry.motorsim.RocketScience;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

public class HTMLExporter {
    static final int WIDTH = 400;
    static final int HEIGHT = 200;

    @SuppressWarnings("deprecation")
    private static String encode(String s) {
        return URLEncoder.encode(s).replace("%B2", "%C2%B2");
    }

    private static <X extends Quantity, Y extends Quantity> String toChart(
            final GraphSimplifier<Duration, Force> source,
            final Iterator<Amount<X>> domain)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        NumberFormat nf = new DecimalFormat("#.##");
        Method f = source.getClass().getMethod("value", Amount.class);

        double xMin = 0, xMax = 0;
        double yMin = 0, yMax = 0;

        StringBuilder xVals = new StringBuilder();
        StringBuilder yVals = new StringBuilder();

        while (domain.hasNext()) {
            Amount<X> aX = domain.next();
            double x = aX.doubleValue((Unit<X>) SI.SECOND);
            @SuppressWarnings("unchecked")
            double y = ((Amount<Y>) f.invoke(source, aX)).doubleValue((Unit<Y>) SI.NEWTON);
            xMin = Math.min(x, xMin);
            xMax = Math.max(x, xMax);
            yMin = y < yMin ? y : yMin;
            yMax = y > yMax ? y : yMax;

            xVals.append(nf.format(x));
            xVals.append(",");
            yVals.append(nf.format(y));
            yVals.append(",");
        }
        xVals.deleteCharAt(xVals.length() - 1);
        yVals.deleteCharAt(yVals.length() - 1);

        // Get the non-preferred Y Unit
        Unit<Y> yUnit2 = RocketScience.UnitPreference.SI
                .getPreferredUnit((Unit<Y>) SI.NEWTON);
        // if ( yUnit2.equals(yUnit) )
        yUnit2 = RocketScience.UnitPreference.NONSI.getPreferredUnit((Unit<Y>) SI.NEWTON);
        double y2Min, y2Max;
        y2Min = Amount.valueOf(yMin, (Unit<Y>) SI.NEWTON).doubleValue(yUnit2);
        y2Max = Amount.valueOf(yMax, (Unit<Y>) SI.NEWTON).doubleValue(yUnit2);

        // Get the non-preferred X Unit
        Unit<X> xUnit2 = RocketScience.UnitPreference.SI
                .getPreferredUnit((Unit<X>) SI.SECOND);
        // if ( xUnit2.equals(xUnit) )
        xUnit2 = RocketScience.UnitPreference.NONSI.getPreferredUnit((Unit<X>) SI.SECOND);
        double x2Min, x2Max;
        x2Min = Amount.valueOf(xMin, (Unit<X>) SI.SECOND).doubleValue(xUnit2);
        x2Max = Amount.valueOf(xMax, (Unit<X>) SI.SECOND).doubleValue(xUnit2);

        boolean x2 = !((Unit<X>) SI.SECOND).equals(xUnit2);
        boolean y2 = x2 || !((Unit<Y>) SI.NEWTON).equals(yUnit2);

        StringBuffer sb = new StringBuffer();
        sb.append("<img src='");
        sb.append("http://chart.apis.google.com/chart?chxt=x,x,y,y").append(y2 ? ",r,r" : "").append(x2 ? ",t,t" : "").append("&chs=").append(WIDTH).append("x").append(HEIGHT).append("&cht=lxy&chco=3072F3");
        sb.append("&chds=").append(nf.format(xMin)).append(",").append(nf.format(xMax)).append(",").append(nf.format(yMin)).append(",").append(nf.format(yMax));
        sb.append("&chxr=" + "0,").append(nf.format(xMin)).append(",").append(nf.format(xMax)).append("|2,").append(nf.format(yMin)).append(",").append(nf.format(yMax));
        if (y2)
            sb.append("|4,").append(nf.format(y2Min)).append(",").append(nf.format(y2Max));
        if (x2)
            sb.append("|6,").append(nf.format(x2Min)).append(",").append(nf.format(x2Max));
        sb.append("&chd=t:").append(xVals.toString()).append("|").append(yVals.toString());
        sb.append("&chxl=1:|").append(encode(SI.SECOND.toString())).append("|3:|").append(encode(SI.NEWTON.toString()));
        if (y2)
            sb.append("|5:|" + encode(yUnit2.toString()));
        if (x2)
            sb.append("|7:|" + encode(xUnit2.toString()));
        sb.append("&chxp=1,50|3,50");
        if (y2)
            sb.append("|5,50");
        if (x2)
            sb.append("|7,50");
        sb.append("' width='" + WIDTH + "' height='" + HEIGHT + "' alt='" + null + "' />");

        return sb.toString();
    }

    public static void export(Burn b, OutputStream os) throws Exception {
        PrintWriter out = new PrintWriter(os);

        BurnSummary bs = new BurnSummary(b);

        out.println("<!--Begin motor " + b.getMotor().getName() + " HTML export from MotorSim-->");
        out.print("<table class='motor' style='text-align: left; border: 1px solid black'>");

        out.print("<tr>");
        out.print("<th colspan='6' class='title'>" + b.getMotor().getName() + "</th>");
        out.print("</tr>");

        out.print("<tr class='summary'>");

        out.print("<th>Rating:</th>");
        out.print("<td>" + bs.getRating() + "</td>");


        out.print("<th>Max Pressure:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.maxPressure())
                + "</td>");

        out.print("</tr>");


        out.print("<tr class='summary'>");

        out.print("<th>Total Impulse:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.totalImpulse())
                + "</td>");


        out.print("<th>Specific Impulse:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.specificImpulse())
                + "</td>");
        out.print("</tr>");

        out.print("<tr class='summary'>");

        out.print("<th>Max Thrust:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.maxThrust())
                + "</td>");

        out.print("<th>Volume Loading:</th>");
        out.print("<td>"
                + (int) (bs.getVolumeLoading() * 100)
                + "%</td>");
        out.print("</tr>");

        out.print("<tr class='summary'>");

        out.print("<th>Average Thrust:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.averageThrust())
                + "</td>");
        out.print("<th>Fuel Mass:</th>");
        out.print("<td>"
                + RocketScience.ammountToRoundedString(bs.getPropellantMass())
                + "</td>");

        out.print("</tr>");


        out.print("<tr>");
        out.print("<td colspan='4' class='thrust'>");
        GraphSimplifier<Duration, Force> thrust = new GraphSimplifier<Duration, Force>(
                b, "thrust", b.getData().keySet().iterator());

        out.print(toChart(thrust, thrust
                .getDomain().iterator()));
        out.print("</td>");
		/*
		out.print("<td colspan='3' class='pressure'>");
		GraphSimplifier<Duration, Pressure> pressure = new GraphSimplifier<Duration, Pressure>(
				b, "pressure", b.getData().keySet().iterator());

		out.print(toChart(SI.SECOND,
				javax.measure.unit.SI.MEGA(javax.measure.unit.SI.PASCAL),
				pressure, "value", pressure.getDomain().iterator(), "Pressure"));

		out.print("</td>");
		*/
        out.print("</tr>");
		/*
		out.print("<tr>");
		out.print("<th colspan='3'>.ENG File</th>");
		out.print("<th colspan='3'>MotorSim File</th>");
		out.print("</tr>");
		out.print("<tr>");
		out.print("<td colspan='3'>");
		out.print("<textarea>");
		out.flush();
		os.flush();
		ENGExporter.export(b, os);
		os.flush();
		out.print("</textarea>");
		out.print("</td>");
		out.print("<td colspan='3'>");
		out.print("<textarea>");
		out.print(MotorIO.writeMotor(b.getMotor()).replace("<", "&lt;").replace(">", "&gt;"));
		out.print("</textarea>");
		out.print("</td>");
		out.print("</tr>");
		*/
        out.print("</table>");

        out.println("\n<!--End motor " + b.getMotor().getName() + "-->");
        out.flush();
        out.close();
    }

}
