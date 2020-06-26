package com.billkuker.rocketry.motorsim;

import com.billkuker.rocketry.motorsim.gui.visual.workbench.MotorWorkbench;

import javax.swing.*;
import java.util.Locale;


public class MotorSim {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name",
                    "MotorSim");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        new Splash("splash.png", 2000);
        final MotorWorkbench mw = new MotorWorkbench();
        Thread.sleep(2000);
        SwingUtilities.invokeLater(() -> mw.setVisible(true));


    }

}
