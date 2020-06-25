package com.billkuker.rocketry.motorsim.gui.visual;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ClassChooser<T> extends JComboBox<T> {
    private static final long serialVersionUID = 1L;
    private final Map<Class<? extends T>, T> lastVal = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ClassChooser(Collection<Class<? extends T>> options, T current) {
        super(new DefaultComboBoxModel<>());
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) getModel();
        for (Class<? extends T> clazz : options) {
            Element e = new Element(clazz);
            model.addElement((T) e);
            if (clazz == current.getClass())
                setSelectedItem(e);
        }
        lastVal.put((Class<? extends T>) current.getClass(), current);

        addActionListener(e -> {
            Class<? extends T> selected = ((Element) Objects.requireNonNull(getSelectedItem())).clazz;
            lastVal.put(selected, classSelected(selected, lastVal.get(selected)));
        });

    }

    protected abstract T classSelected(Class<? extends T> clazz, T last);

    private class Element {
        private final Class<? extends T> clazz;

        Element(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }
}
