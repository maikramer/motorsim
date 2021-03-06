package com.billkuker.rocketry.motorsim.gui.fuel;

import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.gui.visual.Chart;
import com.billkuker.rocketry.motorsim.gui.visual.Editor;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.swing.*;
import java.awt.*;

public abstract class AbstractFuelEditor extends JSplitPane {
    private static final long serialVersionUID = 1L;

    private final JSplitPane editTop;
    private final Fuel f;
    private Chart<Pressure, Velocity> burnRate;

    public AbstractFuelEditor(Fuel f) {
        super(HORIZONTAL_SPLIT);
        this.f = f;

        editTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        editTop.setTopComponent(new Editor(f));
        editTop.setBottomComponent(new Editor(f.getCombustionProduct()));

        JSplitPane editParent = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        setLeftComponent(editParent);
        editParent.setTopComponent(editTop);
        editParent.setBottomComponent(getBurnrateEditComponent());

        setResizeWeight(0);
        setDividerLocation(.3);
        editParent.setDividerLocation(.5);
        editTop.setDividerLocation(.5);
        editParent.resetToPreferredSizes();
        revalidate();

        update();
    }

    protected abstract Component getBurnrateEditComponent();

    public Fuel getFuel() {
        return f;
    }


    protected void update() {
        SwingUtilities.invokeLater(() -> {
            editTop.setTopComponent(new Editor(f));
            editTop.setBottomComponent(new Editor(f.getCombustionProduct()));
            if (burnRate != null)
                AbstractFuelEditor.this.remove(burnRate);
            try {
                burnRate = new Chart<>(
                        SI.MEGA(SI.PASCAL), SI.MILLIMETER.divide(SI.SECOND)
                        .asType(Velocity.class), f, "burnRate", "Chamber Pressure", "Burn Rate");
            } catch (NoSuchMethodException e) {
                throw new Error(e);
            }
            burnRate.setDomain(burnRate.new IntervalDomain(Amount.valueOf(
                    0, SI.MEGA(SI.PASCAL)), Amount.valueOf(11, SI
                    .MEGA(SI.PASCAL)), 50));
            AbstractFuelEditor.this.setRightComponent(burnRate);
            AbstractFuelEditor.this.revalidate();
        });
    }

}
