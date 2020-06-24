package com.billkuker.rocketry.motorsim;

import com.billkuker.rocketry.motorsim.gui.visual.workbench.MotorWorkbench;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

class Splash extends JWindow {
    private static final long serialVersionUID = 1L;

    public Splash(String resName, int waitTime) {
        super((Frame) null);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Image img = null;
        try {
            img = ImageIO.read(Objects.requireNonNull(classloader.getResource(resName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert img != null;
        JLabel l = new JLabel(new ImageIcon(img));
        add(l);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
        final int pause = waitTime;
        setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(pause);
                SwingUtilities.invokeAndWait(() -> {
                    setVisible(false);
                    dispose();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "SplashThread").start();
    }

    public static void main(String[] args) {
        new Splash("splash.png", 1000);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        try {
            g.drawString("Version " + MotorWorkbench.getVersion(), 140, 150);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
