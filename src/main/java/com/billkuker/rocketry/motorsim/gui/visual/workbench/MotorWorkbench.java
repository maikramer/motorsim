package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import com.billkuker.rocketry.motorsim.RocketScience.UnitPreference;
import com.billkuker.rocketry.motorsim.gui.debug.DebugFrame;
import com.billkuker.rocketry.motorsim.gui.fuel.FuelsEditor;
import com.billkuker.rocketry.motorsim.gui.visual.RememberJFrame;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class MotorWorkbench extends RememberJFrame {
    private static final long serialVersionUID = 1L;

    private SettingsEditor settings;

    {
        try {
            settings = new SettingsEditor(this);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private About about;

    {
        try {
            about = new About(this);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private FuelsEditor fuelEditor;
    private final JFrame fuelEditorFrame = new RememberJFrame(800, 600) {
        private static final long serialVersionUID = 1L;

        {
            setIconImage(getIcon());
            setSize(800, 600);
            add(fuelEditor = new FuelsEditor(this));
            JMenuBar b;
            setJMenuBar(b = new JMenuBar());
            b.add(fuelEditor.getMenu());
            try {
                setTitle(getFullName() + " - Fuel Editor");
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    };
    private final MotorsEditor motorsEditor;

    public MotorWorkbench() throws IOException {
        super(1024, 768);
        try {
            setTitle(getFullName());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        setIconImage(getIcon());

        motorsEditor = new MotorsEditor(this);
        setContentPane(motorsEditor);

        addMenu();

        MultiMotorThrustChart mb = new MultiMotorThrustChart();
        JFrame allBurns = new JFrame();
        allBurns.setTitle("All Burns");
        allBurns.setSize(800, 600);
        allBurns.add(mb);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                maybeQuit();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }
        });

    }

    public static String getVersion() throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("pom.xml");
        Model model = reader.read(is);
        return model.getVersion();
    }

    public static String getFullName() throws IOException, XmlPullParserException {
        return "MotorSim " + getVersion();
    }

    public static Image getIcon() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return ImageIO.read(Objects.requireNonNull(classloader.getResource("icon.png")));
    }

    private void maybeQuit() {
        if (motorsEditor.hasDirty()) {
            int response = JOptionPane
                    .showConfirmDialog(
                            MotorWorkbench.this,
                            "There are unsaved Motors.\nExit Anyway?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }
        if (fuelEditor.hasDirty()) {
            int response = JOptionPane
                    .showConfirmDialog(
                            MotorWorkbench.this,
                            "There are unsaved Fuels.\nExit Anyway?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }
        MotorWorkbench.this.dispose();
        System.exit(0);
    }

    private void addMenu() {

        setJMenuBar(new JMenuBar() {
            private static final long serialVersionUID = 1L;

            {
                JMenu file = motorsEditor.getMenu();
                file.add(new JSeparator());
                file.add(new JMenuItem("Quit") {
                    private static final long serialVersionUID = 1L;

                    {
                        addActionListener(e -> maybeQuit());
                    }
                });
                add(file);

                add(new JMenu("Settings") {
                    private static final long serialVersionUID = 1L;

                    {
                        ButtonGroup units = new ButtonGroup();
                        JRadioButtonMenuItem sci = new JRadioButtonMenuItem(
                                "SI");
                        JRadioButtonMenuItem nonsci = new JRadioButtonMenuItem(
                                "NonSI");
                        units.add(sci);
                        units.add(nonsci);
                        sci.setSelected(UnitPreference.getUnitPreference() == UnitPreference.SI);
                        nonsci.setSelected(UnitPreference.getUnitPreference() == UnitPreference.NONSI);
                        sci.addActionListener(arg0 -> UnitPreference
                                .setUnitPreference(UnitPreference.SI));
                        nonsci.addActionListener(arg0 -> UnitPreference
                                .setUnitPreference(UnitPreference.NONSI));
                        add(sci);
                        add(nonsci);

                        add(new JSeparator());
                        add(new JMenuItem("Simulation Settings") {
                            private static final long serialVersionUID = 1L;

                            {
                                addActionListener(e -> settings.setVisible(true));
                            }
                        });
                    }
                });
                add(new JMenu("View") {
                    private static final long serialVersionUID = 1L;

                    {
                        add(new JMenuItem("Detach \"All Motors\" tabs") {
                            private static final long serialVersionUID = 1L;

                            {
                                addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent arg0) {
                                        motorsEditor.detach();
                                    }
                                });
                            }
                        });
                        add(new JMenuItem("Show Fuel Editor") {
                            private static final long serialVersionUID = 1L;

                            {
                                addActionListener(arg0 -> {
                                    fuelEditorFrame.setVisible(true);
                                    fuelEditorFrame.toFront();
                                });
                            }
                        });
                    }
                });
                add(new JMenu("Help") {
                    private static final long serialVersionUID = 1L;

                    {
                        add(new JMenuItem("About") {
                            private static final long serialVersionUID = 1L;

                            {
                                addActionListener(e -> about.setVisible(true));
                            }
                        });
                        add(new JSeparator());
                        add(new JMenu("Debug") {
                            private static final long serialVersionUID = 1L;

                            {
                                add(new JMenuItem("Debug Window") {
                                    private static final long serialVersionUID = 1L;

                                    {
                                        addActionListener(e -> new DebugFrame());
                                    }
                                });
                                add(new JMenuItem("Log Window") {
                                    LF5Appender lf5;
                                    private static final long serialVersionUID = 1L;

                                    {
                                        addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                if (lf5 == null) {
                                                    lf5 = new LF5Appender();
                                                    Logger.getRootLogger().addAppender(lf5);
                                                }
                                                lf5.getLogBrokerMonitor().show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


}
