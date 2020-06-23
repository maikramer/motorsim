package com.billkuker.rocketry.motorsim.gui.visual;

import com.billkuker.rocketry.motorsim.RocketScience;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import javax.measure.unit.Unit;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.Vector;

public class Editor extends PropertySheetPanel {
    private static final long serialVersionUID = 1L;
    private static final NumberFormat nf = new DecimalFormat("##########.###");
    private static final Logger log = LogManager.getLogger(Editor.class);

    private final Object obj;

    @SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
    public Editor(Object o) {
        obj = o;

        PropertyEditorManager.registerEditor(Amount.class,
                AmountPropertyEditor.class);

        setToolBarVisible(false);
        //setMinimumSize(new Dimension(150,200));

        getRendererRegistry().registerRenderer(Amount.class, AmountRenderer.class);

        // Build the list of properties we want it to edit
        //final PropertySheetPanel ps = new PropertySheetPanel();
        PropertyDescriptor[] props;
        try {
            props = Introspector.getBeanInfo(obj.getClass())
                    .getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new Error(e);
        }
        Vector<PropertyDescriptor> v = new Vector<>();
        for (PropertyDescriptor prop : props) {
            if (prop.getName().equals("class"))
                continue;
            v.add(prop);

            if (Enum.class.isAssignableFrom(prop.getPropertyType())) {
                getEditorRegistry().registerEditor(prop.getPropertyType(), new EnumPropertyEditor(prop.getPropertyType()));
            }

        }
        setProperties(v.toArray(new PropertyDescriptor[0]));

        readFromObject(obj);

        getTable().setRowHeight(35);

        setMinimumSize(new Dimension(
                getTable().getPreferredSize().width,
                getTable().getPreferredSize().height + 10));

        addPropertySheetChangeListener(evt -> {
            // When something changes just update the
            // object, I want the changes to be immediate.
            try {
                log.debug("Writing properties to object.");
                writeToObject(obj);
            } catch (Exception v1) {
                log.error(v1);
                java.awt.Toolkit.getDefaultToolkit().beep();
            } finally {
                readFromObject(obj);
            }
        });
    }

    public void showAsWindow() {
        JFrame f = new JFrame();
        f.setTitle(obj.getClass().getName());
        f.setSize(600, 400);
        f.setContentPane(this);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    /*
        public static void main(String args[]){
            Schedule40 o = new Schedule40();
            o.setLength(Amount.valueOf(100, SI.MILLIMETER));
            Editor e = new Editor(o);
            e.showAsWindow();
        }
        */
    public static class AmountRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            return new JLabel(RocketScience.ammountToString((Amount<?>) value));
        }

    }

    public static class AmountPropertyEditor extends PropertyEditorSupport {
        JTextField editor = new JTextField();
        Unit<?> oldUnit;

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public String getAsText() {
            return editor.getText();
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            editor.setText(text);
        }

        @Override
        public Object getValue() {
            String text = editor.getText().trim();

            // Trying to determine if the value is integer or
            // has a decimal part will prevent the uncertainty
            // term from appearing when user types an exact value
            try {
                try {
                    return Amount.valueOf(Integer.parseInt(text), oldUnit);
                } catch (NumberFormatException ignored) {

                }
                return Amount.valueOf(Double.parseDouble(text), oldUnit);
            } catch (NumberFormatException e) {
                // Storing the old unit allows you to type 10 into a field
                // that says 20 mm and get 10 mm, so you dont have to
                // type the unit if they havn't changed.

                //Amount wants a leading 0
                if (text.startsWith(".")) {
                    text = "0" + text;
                }

                Amount<?> a = Amount.valueOf(text);
                oldUnit = a.getUnit();
                return a;
            }

        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void setValue(Object o) {
            Amount a = (Amount) o;
            oldUnit = a.getUnit();

            String text;
            //Leave off the fractional part if it is not relevant
            if (a.isExact())
                text = a.getExactValue() + " " + a.getUnit();
            else
                text = nf.format(a.doubleValue(a.getUnit())) + " " + a.getUnit();

            setAsText(text);
        }

        @Override
        public Component getCustomEditor() {
            return editor;
        }
    }

    public class EnumPropertyEditor<E extends Enum<E>> extends PropertyEditorSupport {
        JComboBox<E> editor = new JComboBox<>() {
            private static final long serialVersionUID = 1L;

            {
                addActionListener(e -> getTable().commitEditing());
            }
        };
        DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
        Class<E> clazz;

        public EnumPropertyEditor(Class<E> clazz) {
            this.clazz = clazz;
            for (E e : EnumSet.allOf(clazz)) {
                model.addElement(e);
            }
            editor.setModel(model);
        }

        @Override
        public Object getValue() {
            return editor.getSelectedItem();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return editor;
        }
    }

}
