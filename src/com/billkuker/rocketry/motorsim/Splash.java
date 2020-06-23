package com.billkuker.rocketry.motorsim;

import com.billkuker.rocketry.motorsim.gui.visual.workbench.MotorWorkbench;

import javax.swing.*;
import java.awt.*;

class Splash extends JWindow {
    private static final long serialVersionUID = 1L;

    public Splash(String resName, int waitTime) {
        super();
        JLabel l = new JLabel(new ImageIcon(resName));
        add(l);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
        final int pause = waitTime;
        setVisible(true);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(pause);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            setVisible(false);
                            dispose();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        g.drawString("Version " + MotorWorkbench.version, 140, 150);
    }
}
