package com.billkuker.rocketry.motorsim.gui.visual.workbench;

import com.billkuker.rocketry.motorsim.*;
import com.billkuker.rocketry.motorsim.aspects.ChangeListening;
import com.billkuker.rocketry.motorsim.cases.Schedule40;
import com.billkuker.rocketry.motorsim.cases.Schedule80;
import com.billkuker.rocketry.motorsim.fuel.FuelResolver;
import com.billkuker.rocketry.motorsim.fuel.KNSU;
import com.billkuker.rocketry.motorsim.grain.*;
import com.billkuker.rocketry.motorsim.gui.Colors;
import com.billkuker.rocketry.motorsim.gui.visual.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

public class MotorEditor extends JPanel implements PropertyChangeListener, FuelResolver.FuelsChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LogManager.getLogger(MotorEditor.class);
    //private static final int XML_TAB = 0;
    private static final int CASING_TAB = 0;
    private static final int GRAIN_TAB = 1;
    private static final int BURN_TAB = 2;
    private static int idx;
    private final Vector<BurnWatcher> burnWatchers = new Vector<>();
    private final DefaultComboBoxModel<Fuel> availableFuels = new DefaultComboBoxModel<>();
    private final List<Class<? extends Grain>> grainTypes = new Vector<>();
    private final List<Class<? extends Chamber>> chamberTypes = new Vector<>();
    Motor motor;
    GrainEditor grainEditor;
    BurnTab bt;
    Burn burn;
    SummaryPanel sp;
    JTextArea error;
    JTabbedPane tabs;
    boolean closed = false;

    {
        grainTypes.add(CoredCylindricalGrain.class);
        grainTypes.add(Finocyl.class);
        grainTypes.add(Star.class);
        grainTypes.add(Moonburner.class);
        grainTypes.add(RodAndTubeGrain.class);
        grainTypes.add(CSlot.class);
        grainTypes.add(EndBurner.class);
        grainTypes.add(MultiPort.class);
        grainTypes.add(Square.class);
    }

    {
        chamberTypes.add(CylindricalChamber.class);
        chamberTypes.add(Schedule40.class);
        chamberTypes.add(Schedule80.class);
    }

    public MotorEditor(Motor m) {
        setLayout(new BorderLayout());
        tabs = new JTabbedPane(JTabbedPane.TOP);
        add(tabs, BorderLayout.CENTER);
        availableFuels.addElement(m.getFuel());
        availableFuels.setSelectedItem(m.getFuel());
        FuelResolver.addFuelsChangeListener(this);
        fuelsChanged();
        setMotor(m);

        Burn.getBurnSettings().addPropertyChangeListener(this);
    }

    public static Motor defaultMotor() {
        Motor m = new Motor();
        m.setName("New Motor " + ++idx);
        m.setManufacturer("MF");
        try {
            m.setFuel(FuelResolver.getFuel(new URI("motorsim:KNDX")));
        } catch (Exception e) {
            throw new Error(e);
        }

        CylindricalChamber c = new CylindricalChamber();
        c.setLength(Amount.valueOf(420, SI.MILLIMETER));
        c.setInnerDiameter(Amount.valueOf(70, SI.MILLIMETER));
        c.setOuterDiameter(Amount.valueOf(72, SI.MILLIMETER));
        m.setChamber(c);

        CoredCylindricalGrain g = new CoredCylindricalGrain();
        try {
            g.setLength(Amount.valueOf(100, SI.MILLIMETER));
            g.setOuterDiameter(Amount.valueOf(62, SI.MILLIMETER));
            g.setInnerDiameter(Amount.valueOf(20, SI.MILLIMETER));
        } catch (PropertyVetoException v) {
            throw new Error(v);
        }

        MultiGrain mg = new MultiGrain(g, 4);
        mg.setSpacing(Amount.valueOf(6, SI.MILLIMETER));
        m.setGrain(mg);

        ConvergentDivergentNozzle n = new ConvergentDivergentNozzle();
        n.setThroatDiameter(Amount.valueOf(14.089, SI.MILLIMETER));
        n.setExitDiameter(Amount.valueOf(44.55, SI.MILLIMETER));
        n.setEfficiency(.85);
        m.setNozzle(n);
        m.setCasingWeight(Amount.valueOf(0, SI.KILOGRAM));

        return m;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Vector<Fuel> ff = new Vector<>();
        ff.add(new KNSU());
    }

    @Override
    public void fuelsChanged() {
        while (availableFuels.getSize() > 0 && availableFuels.getIndexOf(availableFuels.getSelectedItem()) != 0)
            availableFuels.removeElementAt(0);
        while (availableFuels.getSize() > 1)
            availableFuels.removeElementAt(1);
        for (Fuel f : FuelResolver.getFuelMap().values()) {
            if (f != availableFuels.getSelectedItem())
                availableFuels.addElement(f);
        }
    }

    public Motor getMotor() {
        return motor;
    }


    private void setMotor(Motor m) {
        if (motor != null)
            motor.removePropertyChangeListener(this);
        motor = m;
        motor.addPropertyChangeListener(this);
        if (grainEditor != null)
            remove(grainEditor);
        while (tabs.getTabCount() > 1)
            tabs.removeTabAt(1);
        tabs.add(new CaseEditor(), CASING_TAB);
        tabs.add(new GrainEditor(motor.getGrain()), GRAIN_TAB);
        tabs.add(bt = new BurnTab(), BURN_TAB);
    }

    public void addBurnWatcher(BurnWatcher bw) {
        burnWatchers.add(bw);
    }

    @Deprecated
    public void showAsWindow() {
        JFrame f = new JFrame();
        f.setSize(1024, 768);
        f.setContentPane(this);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals("Name")) {
            bt.reBurn();
        } else {
            for (BurnWatcher bw : burnWatchers)
                bw.replace(burn, burn);
        }
    }

    private class BurnTab extends JPanel {
        private static final long serialVersionUID = 1L;
        private Thread currentThread;

        public BurnTab() {
            setLayout(new BorderLayout());
            setName("Simulation Results");
            reBurn();
        }

        public void reBurn() {
            removeAll();
            if (error != null) {
                MotorEditor.this.remove(error);
                error = null;
            }
            if (sp != null) {
                MotorEditor.this.remove(sp);
                sp = null;
            }
            if(currentThread != null && currentThread.isAlive()) currentThread.interrupt();
            System.gc();
            currentThread = new Thread() {
                {
                    setName("Burn " + motor.getName());
                    setDaemon(true);
                }

                public void run() {
                    final Thread me = this;
                    try {
                        final Burn b = new Burn(motor);
                        b.addBurnProgressListener(
                                new Burn.BurnProgressListener() {
                                    @Override
                                    public void burnComplete() {
                                    }

                                    @Override
                                    public void setProgress(float f) {
                                        if (currentThread != me) {
                                            log.info("Cancel burn on change");
                                            throw new BurnCanceled();
                                        }
                                        if (closed) {
                                            log.info("Cancel burn on close");
                                            throw new BurnCanceled();
                                        }
                                    }
                                });

                        MotorEditor.this.add(sp = new SummaryPanel(b), BorderLayout.NORTH);
                        revalidate();
                        b.burn();

                        final BurnPanel bp = new BurnPanel(b);
                        SwingUtilities.invokeLater(new Thread(() -> {
                            add(bp, BorderLayout.CENTER);
                            for (BurnWatcher bw : burnWatchers)
                                bw.replace(burn, b);
                            burn = b;
                            revalidate();
                        }));
                    } catch (BurnCanceled c) {
                        log.info("Burn Canceled!");
                    } catch (final Exception e) {
                        SwingUtilities.invokeLater(new Thread(() -> {
                            if (sp != null)
                                MotorEditor.this.remove(sp);
                            error = new JTextArea(e.getMessage());
                            error.setBackground(Colors.RED);
                            error.setForeground(Color.WHITE);
                            error.setEditable(false);
                            MotorEditor.this.add(error, BorderLayout.NORTH);
                            revalidate();
                        }));
                    }
                }
            };
            currentThread.start();
        }

        private class BurnCanceled extends RuntimeException {
            private static final long serialVersionUID = 1L;
        }
    }

    private class GrainEditor extends JSplitPane {
        private static final long serialVersionUID = 1L;

        public GrainEditor(final Grain g) {
            super(JSplitPane.HORIZONTAL_SPLIT);
            setName("Grain Geometry");
            setRightComponent(new GrainPanel(g));
            if (g instanceof Grain.Composite) {
                final JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

                Editor grainEditor = new Editor(g);
                grainEditor.setAlignmentX(LEFT_ALIGNMENT);
                p.add(grainEditor);

                for (Grain gg : ((Grain.Composite) g).getGrains()) {
                    final int grainEditorIndex = p.getComponentCount() + 2;

                    JLabel l = new JLabel("Grain Type:");
                    l.setAlignmentX(LEFT_ALIGNMENT);
                    p.add(l);

                    p.add(new ClassChooser<Grain>(grainTypes, gg) {
                        private static final long serialVersionUID = 1L;

                        {
                            setAlignmentX(LEFT_ALIGNMENT);
                        }

                        @Override
                        protected Grain classSelected(
                                Class<? extends Grain> clazz, Grain ng) {
                            if (ng == null) {
                                try {
                                    ng = clazz.getDeclaredConstructor().newInstance();
                                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (g instanceof MultiGrain) {
                                ((MultiGrain) g).setGrain(ng);
                                p.remove(grainEditorIndex);
                                p.add(new Editor(ng), grainEditorIndex);
                                p.remove(0);
                                p.add(new Editor(g), 0);
                            }
                            return ng;

                        }
                    });

                    Editor ggEditor = new Editor(gg);
                    ggEditor.setAlignmentX(LEFT_ALIGNMENT);
                    p.add(ggEditor);

                    if (gg instanceof ChangeListening.Subject) {
                        ((ChangeListening.Subject) gg)
                                .addPropertyChangeListener(MotorEditor.this);
                    }
                }
                setLeftComponent(p);
            } else {
                setLeftComponent(new Editor(g));
            }
            if (g instanceof ChangeListening.Subject) {
                ((ChangeListening.Subject) g)
                        .addPropertyChangeListener(MotorEditor.this);
            }
        }
    }

    private class CaseEditor extends JSplitPane implements ComponentListener {
        private static final long serialVersionUID = 1L;
        private final JPanel casing;
        private final JPanel nozzle;
        private HardwarePanel hp;
        private Editor casingEditor;
        private Editor nozzleEditor;

        public CaseEditor() {
            super(JSplitPane.VERTICAL_SPLIT);
            setName("General Parameters");
            this.addComponentListener(this);

            JPanel parts = new JPanel();
            parts.setLayout(new BoxLayout(parts, BoxLayout.X_AXIS));
            setTopComponent(parts);

            JPanel nameAndFuel = new JPanel();
            nameAndFuel.setLayout(new BoxLayout(nameAndFuel, BoxLayout.Y_AXIS));

            AddField(nameAndFuel, "Name:", () -> motor.getName(), (Object value) -> motor.setName((String) value), null, null);
            AddField(nameAndFuel, "Manufacturer:", () -> motor.getManufacturer(), (Object value) -> motor.setManufacturer((String) value), null, null);
            JLabel l;

            l = new JLabel("Fuel:");
            l.setAlignmentX(LEFT_ALIGNMENT);
            nameAndFuel.add(l);

            nameAndFuel.add(new JComboBox<Fuel>(availableFuels) {
                private static final long serialVersionUID = 1L;

                {
                    setAlignmentX(LEFT_ALIGNMENT);
                    this.setSelectedItem(motor.getFuel());
                    setMinimumSize(new Dimension(200, 20));
                    setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
                    addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            motor.setFuel((Fuel) getSelectedItem());
                            log.debug("FUEL CHANGED");
                        }
                    });
                }
            });

            l = new JLabel("Casing:");
            l.setAlignmentX(LEFT_ALIGNMENT);
            nameAndFuel.add(l);

            nameAndFuel.add(new ClassChooser<Chamber>(chamberTypes, motor.getChamber()) {
                private static final long serialVersionUID = 1L;

                {
                    setAlignmentX(LEFT_ALIGNMENT);
                    setMinimumSize(new Dimension(200, 20));
                    setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
                }

                @Override
                protected Chamber classSelected(Class<? extends Chamber> clazz, Chamber c) {
                    try {
                        if (c != null) {
                            motor.setChamber(c);
                        } else {
                            motor.setChamber(clazz.getDeclaredConstructor().newInstance());
                        }
                        return motor.getChamber();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        log.error(e);
                    }
                    return null;
                }
            });


            AddField(nameAndFuel, "Delay", () -> motor.getEjectionDelay(), (Object value) -> {
                if (value instanceof Amount<?>) motor.setEjectionDelay((Amount<Duration>) value);
            }, SI.SECOND, null);
            AddField(nameAndFuel, "Weight without Fuel", () -> motor.getCasingWeight(), (Object value) -> {
                if (value instanceof Amount<?>) motor.setCasingWeight((Amount<Mass>) value);
            }, SI.KILOGRAM, null);

            nameAndFuel.add(Box.createVerticalGlue());
            parts.add(nameAndFuel);

            casing = new JPanel();
            casing.setLayout(new BoxLayout(casing, BoxLayout.Y_AXIS));
            l = new JLabel("Casing:");
            l.setAlignmentX(LEFT_ALIGNMENT);
            casing.add(l);
            parts.add(casing);

            nozzle = new JPanel();
            nozzle.setLayout(new BoxLayout(nozzle, BoxLayout.Y_AXIS));
            l = new JLabel("Nozzle:");
            l.setAlignmentX(LEFT_ALIGNMENT);
            nozzle.add(l);
            parts.add(nozzle);

            motor.addPropertyChangeListener(arg0 -> {
                setup();
                setResizeWeight(.5);
                setDividerLocation(.5);
            });

            setup();
        }

        private void AddField(JPanel nameAndFuel, String name, GetValueAction<?> getValueAction,
                              SetValueAction<Object> setValueAction, Unit unit, OnChangeAction onChangeAction) {
            JLabel l = new JLabel(name);
            log.debug("AddField");
            l.setAlignmentX(LEFT_ALIGNMENT);
            nameAndFuel.add(l);
            nameAndFuel.add(new JTextField() {
                private static final long serialVersionUID = 1L;

                {
                    Object initialValue = getValueAction.getValue();
                    if (unit != null && initialValue instanceof Amount<?>) {
                        Amount<?> initialAmount = (Amount<?>) initialValue;
                        setText("" + initialAmount.doubleValue(unit) + " " + unit);
                    } else {
                        setText((String) initialValue);
                    }
                    setAlignmentX(LEFT_ALIGNMENT);
                    setMinimumSize(new Dimension(200, 20));
                    setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
                    final JTextField t = this;
                    final String FROMFOCUSLOST = "FromFocusLlost";
                    final boolean[] fromAction = {false};
                    t.addFocusListener(new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent focusEvent) {
                            if (unit != null) {
                                Amount<?> amount = (Amount<?>) getValueAction.getValue();
                                t.setText(String.format("%.3f", amount.doubleValue(unit)));
                            }
                            selectAll();
                        }

                        @Override
                        public void focusLost(FocusEvent focusEvent) {
                            if (fromAction[0]) {
                                fromAction[0] = false;
                            } else {
                                System.out.println("seting");
                                setActionCommand(FROMFOCUSLOST);
                                postActionEvent();
                            }
                        }
                    });

                    addActionListener(actionEvent -> {
                        System.out.println("Command: " + actionEvent.getActionCommand());
                        String n = t.getText();
                        Object objectValue = getValueAction.getValue();
                        if (!n.isEmpty()) {
                            if (unit != null && objectValue instanceof Amount<?>) {
                                double doubleValue;
                                Amount<?> amount = (Amount<?>) objectValue;
                                try {
                                    String corrected = n.replace(',', '.');
                                    doubleValue = Double.parseDouble(corrected);
                                } catch (NumberFormatException e) {
                                    t.setText("" + String.format("%.2f", amount.doubleValue(unit)) + " " + unit);
                                    if (!actionEvent.getActionCommand().equals(FROMFOCUSLOST)) {
                                        fromAction[0] = true;
                                        t.transferFocusUpCycle();
                                    }
                                    return;
                                }

                                Amount<?> amountValue = Amount.valueOf(doubleValue, unit);
                                if (amount != amountValue) {
                                    setValueAction.setValue(amountValue);
                                    if (onChangeAction != null) {
                                        onChangeAction.call();
                                    }
                                    t.setText("" + String.format("%.2f", amountValue.doubleValue(unit)) + " " + unit);
                                }
                            } else if (objectValue != n) {
                                setValueAction.setValue(n);
                                if (onChangeAction != null) {
                                    onChangeAction.call();
                                }
                                t.setText(((String) getValueAction.getValue()).trim());
                            }
                        }
                        if (!actionEvent.getActionCommand().equals(FROMFOCUSLOST)) {
                            fromAction[0] = true;
                            t.transferFocusUpCycle();
                        }

                    });

                }
            });
        }

        private void setup() {
            if (casingEditor != null)
                casing.remove(casingEditor);
            casingEditor = new Editor(motor.getChamber());
            casingEditor.setAlignmentX(LEFT_ALIGNMENT);
            casing.add(casingEditor);

            if (nozzleEditor != null)
                nozzle.remove(nozzleEditor);
            nozzleEditor = new Editor(motor.getNozzle());
            nozzleEditor.setAlignmentX(LEFT_ALIGNMENT);
            nozzle.add(nozzleEditor);

            if (hp != null)
                remove(hp);
            setBottomComponent(hp = new HardwarePanel(motor));
            if (motor.getNozzle() instanceof ChangeListening.Subject) {
                ((ChangeListening.Subject) motor.getNozzle())
                        .addPropertyChangeListener(MotorEditor.this);
            }
            if (motor.getChamber() instanceof ChangeListening.Subject) {
                ((ChangeListening.Subject) motor.getChamber())
                        .addPropertyChangeListener(MotorEditor.this);
            }
            if (motor.getFuel() instanceof ChangeListening.Subject) {
                ((ChangeListening.Subject) motor.getFuel())
                        .addPropertyChangeListener(MotorEditor.this);
            }
        }

        @Override
        public void componentHidden(ComponentEvent arg0) {

        }

        @Override
        public void componentMoved(ComponentEvent arg0) {

        }

        @Override
        public void componentResized(ComponentEvent arg0) {
            setResizeWeight(.5);
            setDividerLocation(.5);
        }

        @Override
        public void componentShown(ComponentEvent arg0) {

        }
    }

    public interface GetValueAction<T> {
        T getValue();
    }

    public interface SetValueAction<T> {
        void setValue(T value);
    }

    public interface OnChangeAction {
        void call();
    }

}
