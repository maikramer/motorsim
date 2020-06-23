package com.billkuker.rocketry.motorsim.gui.debug;

import com.billkuker.rocketry.motorsim.gui.visual.RememberJFrame;

import javax.swing.*;

public class DebugFrame extends RememberJFrame {
    private static final long serialVersionUID = 1L;

    public DebugFrame() {
        super(800, 600);
        setTitle("MotorSim - Debug");
        JTabbedPane tabs = new JTabbedPane();
        setContentPane(tabs);
        tabs.add("Threads", new JScrollPane(new ThreadsPanel()));
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
