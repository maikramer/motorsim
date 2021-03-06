package com.billkuker.rocketry.motorsim.gui.visual;

import com.billkuker.rocketry.motorsim.Chamber;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Nozzle;
import com.billkuker.rocketry.motorsim.aspects.ChangeListening;
import com.billkuker.rocketry.motorsim.gui.visual.workbench.MotorEditor;
import org.jscience.physics.amount.Amount;

import javax.measure.unit.SI;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.beans.PropertyChangeListener;

public class HardwarePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    final Motor m;


    public HardwarePanel(Motor m) {
        this.m = m;
        Nozzle nozzle = m.getNozzle();
        Chamber chamber = m.getChamber();
        PropertyChangeListener repainter = evt -> repaint();
        if (nozzle instanceof ChangeListening.Subject) {
            ((ChangeListening.Subject) nozzle).addPropertyChangeListener(repainter);
        }
        if (chamber instanceof ChangeListening.Subject) {
            ((ChangeListening.Subject) chamber).addPropertyChangeListener(repainter);
        }
    }

    public static void main(String[] args) {
        Motor m = MotorEditor.defaultMotor();
        new HardwarePanel(m).showAsWindow();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(10, 10);

        g2d.setColor(Color.black);

        Nozzle nozzle = m.getNozzle();
        Chamber chamber = m.getChamber();

        Shape c = chamber.chamberShape();

        Shape n = nozzle.nozzleShape(Amount.valueOf(c.getBounds().getWidth(), SI.MILLIMETER));


        Rectangle cb = c.getBounds();
        Rectangle nb = n.getBounds();
        double w, h;
        w = Math.max(cb.getWidth(), nb.getWidth());
        h = cb.getHeight() + nb.getHeight();

        double mw, mh;
        mw = getHeight() - 10;
        mh = getWidth() - 10;

        double sw, sh, s;
        sw = mw / w;
        sh = mh / h;
        s = Math.min(sw, sh);

        g2d.rotate(-Math.PI / 2);

        g2d.translate(0, -cb.getY() - 5);
        g2d.scale(s, s);
        g2d.translate(-(getHeight() / (s * 2)), 0);

        g2d.setStroke(new BasicStroke(1));
        g2d.draw(c);

        g2d.translate(0, cb.getHeight());

        g2d.draw(n);

        Shape grain = m.getGrain().getSideView(Amount.valueOf(0, SI.MILLIMETER));
        Shape grain2 = m.getGrain().getSideView(Amount.valueOf(1, SI.MILLIMETER));
        Area burning = new Area(grain);
        burning.subtract(new Area(grain2));
        Rectangle gb = grain.getBounds();
        double x = -gb.getMaxX() + gb.getWidth() / 2.0;
        g2d.translate(x, -gb.getMaxY());
        g2d.draw(grain);
        g2d.setColor(Color.GRAY);
        g2d.fill(grain);
        g2d.setColor(Color.RED);
        g2d.fill(burning);
    }

    public void showAsWindow() {
        JFrame f = new JFrame();
        f.setSize(220, 250);
        f.setContentPane(this);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }
}
