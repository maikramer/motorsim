package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.Validating;
import com.billkuker.rocketry.motorsim.Validating.ValidationException;
import com.billkuker.rocketry.motorsim.visual.workbench.WorkbenchTreeModel.FuelNode;

public class WorkbenchTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, final Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		String tip = null;
		setTextNonSelectionColor(Color.black);
		setTextSelectionColor(Color.white);
		
		Object part = null;
		if (value instanceof DefaultMutableTreeNode) {
			part = ((DefaultMutableTreeNode) value).getUserObject();
		}
		
		if ( part instanceof Validating ){
				try {
					((Validating)part).validate();
				} catch (ValidationException e) {
					setTextSelectionColor(Color.RED);
					setTextNonSelectionColor(Color.RED);
					setToolTipText(e.getMessage());
					tip = e.getMessage();
				}
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (part instanceof Motor) {
			setText(((Motor) part).getName());
		} else if ( value instanceof FuelNode ){
			setText(((FuelNode)value).getFuel().getName());
		} else if ( part instanceof String ) {
			setText((String)part);
		} else if ( part == null ) {
			setText("");
		} else {
			setText(part.getClass().getSimpleName());
		}
		setToolTipText(tip);
		


		return this;
	}
}
