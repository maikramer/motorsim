package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.gui.visual.MultiObjectEditor;
import com.billkuker.rocketry.motorsim.gui.visual.RememberJFrame;
import com.billkuker.rocketry.motorsim.io.ENGExporter;
import com.billkuker.rocketry.motorsim.io.HTMLExporter;
import com.billkuker.rocketry.motorsim.io.MotorIO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Vector;

public class MotorsEditor extends MultiObjectEditor<Motor, MotorEditor> {
    public static final String FILE_EXTENSION = ".ms2";
    private static final Logger log = LogManager.getLogger(MotorsEditor.class);
    private static String lastPath = ".";

    private static final long serialVersionUID = 1L;

    MultiMotorThrustChart mbc = new MultiMotorThrustChart();
    MultiMotorPressureChart mpc = new MultiMotorPressureChart();
    JScrollPane mmtScroll;
    MultiMotorTable mmt = new MultiMotorTable();

    JFrame detached;
    JTabbedPane detachedTabs;

    public MotorsEditor(JFrame f) {
        super(f, "Motor");

        mmtScroll = new JScrollPane(mmt);

        addCreator(new ObjectCreator() {
            @Override
            public Motor newObject() {
                return MotorEditor.defaultMotor();
            }

            @Override
            public String getName() {
                return "Motor";
            }
        });

        detached = new RememberJFrame(800, 600) {
            private static final long serialVersionUID = 1L;
        };
        try {
            detached.setIconImage(MotorWorkbench.getIcon());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            detached.setTitle(MotorWorkbench.getFullName() + " - All Motors");
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        detached.setContentPane(detachedTabs = new JTabbedPane());

        detached.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                attach();
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowActivated(WindowEvent arg0) {
            }
        });
        attach();
    }

    public static String getLastPath() {
        try {
            Paths.get(lastPath);
        } catch (InvalidPathException | NullPointerException ex) {
            lastPath = ".";
        }

        return lastPath;
    }

    public static void setLastPath(String lastPath) {
        try {
            Paths.get(lastPath);
        } catch (InvalidPathException | NullPointerException ex) {
            lastPath = ".";
        }
        MotorsEditor.lastPath = lastPath;
    }

    public void attach() {
        detachedTabs.remove(mbc);
        detachedTabs.remove(mpc);
        detachedTabs.remove(mmtScroll);
        insertTab("All Motors", null, mmtScroll, null, 0);
        insertTab("All Thrust", null, mbc, null, 1);
        insertTab("All Pressure", null, mpc, null, 2);
        detached.setVisible(false);
    }

    public void detach() {
        if (detached.isVisible())
            return;
        remove(mbc);
        remove(mpc);
        remove(mmtScroll);
        detachedTabs.addTab("All Motors", mmtScroll);
        detachedTabs.addTab("All Thrust", mbc);
        detachedTabs.addTab("All Pressure", mpc);
        detached.setVisible(true);
    }

    @Override
    protected void objectAdded(Motor m, MotorEditor e) {
        e.addBurnWatcher(mbc);
        e.addBurnWatcher(mpc);
        e.addBurnWatcher(mmt);
    }

    @Override
    protected void objectRemoved(Motor m, MotorEditor e) {
        mbc.removeBurn(e.burn);
        mpc.removeBurn(e.burn);
        mmt.removeBurn(e.burn);
        e.closed = true;
    }

    @Override
    public MotorEditor createEditor(Motor o) {
        return new MotorEditor(o);
    }

    @Override
    protected Motor loadFromFile(File f) throws IOException {
        setLastPath(f.getParent());
        return MotorIO.readMotor(new FileInputStream(f));
    }

    @Override
    protected void saveToFile(Motor o, File f) throws IOException {
        setLastPath(f.getParent());
        MotorIO.writeMotor(o, new FileOutputStream(f));
    }

    @Override
    public JMenu getMenu() {
        JMenu ret = super.getMenu();
        ret.add(new JSeparator());
        ret.add(new JMenu("Export") {
            private static final long serialVersionUID = 1L;

            {
                add(new JMenuItem("Export .ENG") {
                    private static final long serialVersionUID = 1L;

                    {
                        addActionListener(arg0 -> {
                            final FileDialog fd = new FileDialog(frame,
                                    "Export .ENG File", FileDialog.SAVE);
                            String tittle = getTitleAt(getSelectedIndex()).replace(FILE_EXTENSION, "");
                            fd.setFile(tittle +".eng");
                            fd.setDirectory(getLastPath());
                            fd.setFilenameFilter((File dir, String name)->name.endsWith(".eng"));
                            fd.setVisible(true);
                            if (fd.getFile() != null) {
                                File file = new File(fd.getDirectory()
                                        + fd.getFile());
                                setLastPath(fd.getDirectory());
                                MotorEditor me = getSelectedEditor();
                                Vector<Burn> bb = new Vector<>();
                                bb.add(me.burn);
                                try {
                                    ENGExporter.export(bb, file);
                                } catch (IOException e) {
                                    log.error(e);
                                }
                            }
                        });
                    }
                });
                add(new JMenuItem("Export HTML to Clipboard") {
                    private static final long serialVersionUID = 1L;

                    {
                        addActionListener(arg0 -> {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            MotorEditor me = getSelectedEditor();
                            try {
                                HTMLExporter.export(me.burn, out);
                                String html = new String(out.toByteArray());
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                clipboard.setContents(new StringSelection(html), null);
                                JOptionPane.showMessageDialog(MotorsEditor.this, "HTML Copied to Clipboard");
                            } catch (Exception ignored) {

                            }

                        });
                    }
                });
            }
        });
        return ret;
    }
}
