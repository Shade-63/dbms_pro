package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.PatientDAO;
import com.hospital.bedalloc.dao.WaitlistDAO;
import com.hospital.bedalloc.model.Patient;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for manually adding a patient to the waitlist.
 */
public class WaitlistAddDialog extends JDialog {

    private final WaitlistDAO waitlistDAO = new WaitlistDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private boolean saved = false;

    private JComboBox<Patient> patientCombo;
    private JComboBox<String> wardCombo;
    private JSpinner prioritySpinner;
    private JTextArea notesArea;
    private JTextField searchField;

    public WaitlistAddDialog(Frame parent) {
        super(parent, "Add Patient to Waitlist", true);
        setSize(500, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 158, 11));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Add Patient to Waitlist");
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
        gbc.insets = new Insets(7, 4, 7, 4);
        gbc.weightx = 1.0;
        int row = 0;

        // Patient search
        addLabel(form, gbc, row, "Search Patient");
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Type name or contact to filter");
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(searchField, gbc);

        // Patient combo
        addLabel(form, gbc, row, "Select Patient *");
        patientCombo = new JComboBox<>();
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(patientCombo, gbc);

        // Ward
        addLabel(form, gbc, row, "Ward Type *");
        wardCombo = new JComboBox<>(new String[]{"General", "ICU", "Emergency", "Pediatric", "Maternity", "Private", "Semi-Private"});
        wardCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(wardCombo, gbc);

        // Priority
        addLabel(form, gbc, row, "Priority (1–10) *");
        prioritySpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        prioritySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        form.add(prioritySpinner, gbc);

        // Notes
        addLabel(form, gbc, row, "Notes");
        notesArea = new JTextArea(3, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.65;
        form.add(notesScroll, gbc);

        add(form, BorderLayout.CENTER);

        // Search listener: filter patients as user types
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterPatients(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterPatients(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterPatients(); }
        });

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton("Add to Waitlist");
        save.setBackground(new Color(245, 158, 11));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);

        // Load all patients initially
        loadAllPatients();
    }

    private void loadAllPatients() {
        SwingWorkerUtil.execute(
            () -> patientDAO.getAllPatients(),
            new SwingWorkerUtil.Callback<List<Patient>>() {
                @Override public void onComplete(List<Patient> list) {
                    populateCombo(list);
                }
                @Override public void onError(Exception e) {
                    JOptionPane.showMessageDialog(WaitlistAddDialog.this, "Could not load patients: " + e.getMessage());
                }
            }
        );
    }

    private void filterPatients() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadAllPatients(); return; }
        SwingWorkerUtil.execute(
            () -> patientDAO.searchPatients(keyword),
            new SwingWorkerUtil.Callback<List<Patient>>() {
                @Override public void onComplete(List<Patient> list) { populateCombo(list); }
                @Override public void onError(Exception e) {}
            }
        );
    }

    private void populateCombo(List<Patient> patients) {
        patientCombo.removeAllItems();
        for (Patient p : patients) patientCombo.addItem(p);
    }

    private void handleSave() {
        Patient selected = (Patient) patientCombo.getSelectedItem();
        if (selected == null) { showError("Please select a patient."); return; }

        int priority = (int) prioritySpinner.getValue();
        String ward  = (String) wardCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        SwingWorkerUtil.execute(
            () -> waitlistDAO.addToWaitlist(selected.getPatientId(), ward, priority, notes),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { saved = true; dispose(); }
                    else showError("Failed to add patient to waitlist.");
                }
                @Override public void onError(Exception e) {
                    showError("Database error: " + e.getMessage());
                }
            }
        );
    }

    public boolean isSaved() { return saved; }

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lbl, gbc);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
