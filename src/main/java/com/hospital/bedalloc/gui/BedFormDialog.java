package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.BedDAO;
import com.hospital.bedalloc.dao.WardDAO;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.model.Ward;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Reusable Add / Edit dialog for Bed records.
 */
public class BedFormDialog extends JDialog {

    private final BedDAO bedDAO = new BedDAO();
    private final WardDAO wardDAO = new WardDAO();
    private final Bed existing;
    private boolean saved = false;

    private JTextField bedNumberField, equipmentField;
    private JComboBox<Ward> wardCombo;
    private JComboBox<String> statusCombo;

    public BedFormDialog(Frame parent, Bed bed) {
        super(parent, bed == null ? "Add New Bed" : "Edit Bed", true);
        this.existing = bed;

        setSize(460, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 99, 235));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel(bed == null ? "Add New Bed" : "Edit Bed Details");
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

        // Bed Number
        addLabel(form, gbc, row, "Bed Number *");
        bedNumberField = new JTextField();
        bedNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(bedNumberField, gbc);

        // Ward
        addLabel(form, gbc, row, "Ward *");
        wardCombo = new JComboBox<>();
        wardCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(wardCombo, gbc);

        // Equipment Status
        addLabel(form, gbc, row, "Equipment Status");
        equipmentField = new JTextField("Standard");
        equipmentField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(equipmentField, gbc);

        // Current Status (only for Add mode; editing status uses Change Status button)
        if (existing == null) {
            addLabel(form, gbc, row, "Initial Status");
            statusCombo = new JComboBox<>(new String[]{"Available", "Maintenance"});
            statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
            form.add(statusCombo, gbc);
        }

        add(form, BorderLayout.CENTER);

        // Load wards into combo
        loadWards();

        // Pre-populate if editing
        if (existing != null) {
            bedNumberField.setText(existing.getBedNumber());
            equipmentField.setText(existing.getEquipmentStatus());
        }

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton(existing == null ? "Add Bed" : "Save Changes");
        save.setBackground(new Color(37, 99, 235));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadWards() {
        try {
            List<Ward> wards = wardDAO.getAllWards();
            for (Ward w : wards) wardCombo.addItem(w);

            if (existing != null) {
                for (int i = 0; i < wardCombo.getItemCount(); i++) {
                    if (wardCombo.getItemAt(i).getWardId() == existing.getWardId()) {
                        wardCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load wards: " + ex.getMessage());
        }
    }

    private void handleSave() {
        String bedNum = bedNumberField.getText().trim();
        if (bedNum.isEmpty()) { showError("Bed Number is required."); return; }

        Ward selectedWard = (Ward) wardCombo.getSelectedItem();
        if (selectedWard == null) { showError("Please select a ward."); return; }

        Bed b = existing != null ? existing : new Bed();
        b.setBedNumber(bedNum);
        b.setWardId(selectedWard.getWardId());
        b.setEquipmentStatus(equipmentField.getText().trim().isEmpty() ? "Standard" : equipmentField.getText().trim());

        try {
            if (existing == null) {
                b.setCurrentStatus(statusCombo != null ? (String) statusCombo.getSelectedItem() : "Available");
                if (!bedDAO.insertBed(b)) { showError("Failed to add bed."); return; }
            } else {
                if (!bedDAO.updateBed(b)) { showError("Failed to update bed."); return; }
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }

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
