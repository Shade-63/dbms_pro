package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.StaffDAO;
import com.hospital.bedalloc.model.Staff;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Reusable Add / Edit dialog for Staff records.
 */
public class StaffFormDialog extends JDialog {

    private final StaffDAO staffDAO = new StaffDAO();
    private final Staff existing;
    private boolean saved = false;

    private JTextField nameField, departmentField, phoneField;
    private JComboBox<String> roleCombo;

    public StaffFormDialog(Frame parent, Staff staff) {
        super(parent, staff == null ? "Add New Staff" : "Edit Staff", true);
        this.existing = staff;

        setSize(440, 330);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(124, 58, 237));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel(staff == null ? "Add New Staff Member" : "Edit Staff Member");
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
        nameField = addRow(form, gbc, row++, "Full Name *", new JTextField());

        // Role combo
        addLabel(form, gbc, row, "Role *");
        roleCombo = new JComboBox<>(new String[]{"Doctor", "Nurse", "Admin", "Technician", "Receptionist"});
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(roleCombo, gbc);

        departmentField = addRow(form, gbc, row++, "Department", new JTextField());
        phoneField      = addRow(form, gbc, row++, "Phone",      new JTextField());

        add(form, BorderLayout.CENTER);

        if (existing != null) {
            nameField.setText(existing.getName());
            roleCombo.setSelectedItem(existing.getRole());
            departmentField.setText(existing.getDepartment());
            phoneField.setText(existing.getPhone());
        }

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton(existing == null ? "Add Staff" : "Save Changes");
        save.setBackground(new Color(124, 58, 237));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showError("Name is required."); return; }

        Staff s = existing != null ? existing : new Staff();
        s.setName(name);
        s.setRole((String) roleCombo.getSelectedItem());
        s.setDepartment(departmentField.getText().trim());
        s.setPhone(phoneField.getText().trim());

        try {
            if (existing == null) {
                if (!staffDAO.insertStaff(s)) { showError("Failed to add staff."); return; }
            } else {
                if (!staffDAO.updateStaff(s)) { showError("Failed to update staff."); return; }
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }

    private <T extends JTextField> T addRow(JPanel p, GridBagConstraints gbc, int row, String label, T field) {
        addLabel(p, gbc, row, label);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.65;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(field, gbc);
        return field;
    }

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lbl, gbc);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
