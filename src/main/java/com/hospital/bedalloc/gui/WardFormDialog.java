package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.WardDAO;
import com.hospital.bedalloc.model.Ward;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Reusable Add / Edit dialog for Ward records.
 */
public class WardFormDialog extends JDialog {

    private final WardDAO wardDAO = new WardDAO();
    private final Ward existing;
    private boolean saved = false;

    private JTextField wardTypeField, floorField, capacityField, chargeField;

    public WardFormDialog(Frame parent, Ward ward) {
        super(parent, ward == null ? "Add New Ward" : "Edit Ward", true);
        this.existing = ward;

        setSize(440, 340);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(16, 185, 129));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel(ward == null ? "Add New Ward" : "Edit Ward Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Form ────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.weightx = 1.0;

        int row = 0;
        wardTypeField = addRow(form, gbc, row++, "Ward Type *",    new JTextField());
        floorField    = addRow(form, gbc, row++, "Floor *",        new JTextField());
        capacityField = addRow(form, gbc, row++, "Capacity *",     new JTextField());
        chargeField   = addRow(form, gbc, row++, "Daily Charge (₹) *", new JTextField());

        add(form, BorderLayout.CENTER);

        if (existing != null) {
            wardTypeField.setText(existing.getWardType());
            floorField.setText(String.valueOf(existing.getFloor()));
            capacityField.setText(String.valueOf(existing.getCapacity()));
            chargeField.setText(String.valueOf(existing.getDailyCharge()));
        }

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton(existing == null ? "Add Ward" : "Save Changes");
        save.setBackground(new Color(16, 185, 129));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String wType = wardTypeField.getText().trim();
        if (wType.isEmpty()) { showError("Ward Type is required."); return; }

        int floor, capacity;
        double charge;
        try { floor = Integer.parseInt(floorField.getText().trim()); }
        catch (NumberFormatException ex) { showError("Floor must be a valid integer."); return; }
        try { capacity = Integer.parseInt(capacityField.getText().trim()); }
        catch (NumberFormatException ex) { showError("Capacity must be a valid integer."); return; }
        if (capacity <= 0) { showError("Capacity must be greater than 0."); return; }
        try { charge = Double.parseDouble(chargeField.getText().trim()); }
        catch (NumberFormatException ex) { showError("Daily Charge must be a valid number."); return; }
        if (charge < 0) { showError("Daily Charge cannot be negative."); return; }

        Ward w = existing != null ? existing : new Ward();
        w.setWardType(wType);
        w.setFloor(floor);
        w.setCapacity(capacity);
        w.setDailyCharge(charge);

        try {
            if (existing == null) {
                if (!wardDAO.insertWard(w)) { showError("Failed to add ward."); return; }
            } else {
                if (!wardDAO.updateWard(w)) { showError("Failed to update ward."); return; }
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }

    private <T extends JTextField> T addRow(JPanel p, GridBagConstraints gbc, int row, String label, T field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(field, gbc);
        return field;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
