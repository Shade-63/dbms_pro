package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.PatientDAO;
import com.hospital.bedalloc.model.Patient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable Add / Edit dialog for Patient records.
 * Pass null as 'patient' for Add mode; pass an existing Patient for Edit mode.
 */
public class PatientFormDialog extends JDialog {

    private final PatientDAO patientDAO = new PatientDAO();
    private final Patient existing;   // null = Add mode
    private boolean saved = false;

    private JTextField nameField, ageField, contactField, emContactField, addressField;
    private JComboBox<String> bloodGroupCombo;

    public PatientFormDialog(Frame parent, Patient patient) {
        super(parent, patient == null ? "Add New Patient" : "Edit Patient", true);
        this.existing = patient;

        setSize(480, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 99, 235));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel(patient == null ? "Add New Patient" : "Edit Patient Details");
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
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        int row = 0;

        nameField    = addRow(form, gbc, row++, "Full Name *", new JTextField());
        ageField     = addRow(form, gbc, row++, "Age",         new JTextField());

        bloodGroupCombo = new JComboBox<>(new String[]{"O+","O-","A+","A-","B+","B-","AB+","AB-"});
        addComboRow(form, gbc, row++, "Blood Group", bloodGroupCombo);

        contactField  = addRow(form, gbc, row++, "Contact *",          new JTextField());
        emContactField = addRow(form, gbc, row++, "Emergency Contact", new JTextField());
        addressField   = addRow(form, gbc, row++, "Address",           new JTextField());

        add(new JScrollPane(form), BorderLayout.CENTER);

        // Pre-populate if editing
        if (existing != null) {
            nameField.setText(existing.getName());
            ageField.setText(String.valueOf(existing.getAge()));
            bloodGroupCombo.setSelectedItem(existing.getBloodGroup());
            contactField.setText(existing.getContact());
            emContactField.setText(existing.getEmergencyContact());
            addressField.setText(existing.getAddress());
        }

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton(existing == null ? "Add Patient" : "Save Changes");
        save.setBackground(new Color(37, 99, 235));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String name    = nameField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty()) { showError("Full Name is required."); return; }
        if (contact.isEmpty()) { showError("Contact number is required."); return; }

        int age = 0;
        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try { age = Integer.parseInt(ageText); }
            catch (NumberFormatException ex) { showError("Age must be a valid number."); return; }
            if (age <= 0 || age > 150) { showError("Age must be between 1 and 150."); return; }
        }

        Patient p = existing != null ? existing : new Patient();
        p.setName(name);
        p.setAge(age);
        p.setBloodGroup((String) bloodGroupCombo.getSelectedItem());
        p.setContact(contact);
        p.setEmergencyContact(emContactField.getText().trim());
        p.setAddress(addressField.getText().trim());

        try {
            if (existing == null) {
                int id = patientDAO.insertPatient(p);
                if (id < 0) { showError("Failed to add patient."); return; }
            } else {
                if (!patientDAO.updatePatient(p)) { showError("Failed to update patient."); return; }
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private <T extends JTextField> T addRow(JPanel p, GridBagConstraints gbc, int row, String label, T field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 30));
        p.add(field, gbc);
        return field;
    }

    private void addComboRow(JPanel p, GridBagConstraints gbc, int row, String label, JComboBox<?> combo) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(combo, gbc);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
