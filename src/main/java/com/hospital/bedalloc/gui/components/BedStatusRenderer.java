package com.hospital.bedalloc.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BedStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = (value != null) ? value.toString() : "";
        
        if (!isSelected) {
            switch (status) {
                case "Available":
                    c.setForeground(new Color(6, 95, 70));
                    c.setBackground(new Color(209, 250, 229));
                    break;
                case "Occupied":
                    c.setForeground(new Color(153, 27, 27));
                    c.setBackground(new Color(254, 226, 226));
                    break;
                case "Maintenance":
                    c.setForeground(new Color(146, 64, 14));
                    c.setBackground(new Color(254, 243, 199));
                    break;
                default:
                    c.setForeground(table.getForeground());
                    c.setBackground(table.getBackground());
            }
        }
        
        setHorizontalAlignment(JLabel.CENTER);
        return c;
    }
}
