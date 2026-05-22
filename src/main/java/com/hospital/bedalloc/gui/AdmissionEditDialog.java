package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.AdmissionDAO;
import com.hospital.bedalloc.model.Admission;
import com.hospital.bedalloc.util.DateUtil;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import java.awt.*;
import java.sql.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog to edit an active admission's expected discharge date and notes.
 */
public class AdmissionEditDialog extends JDialog {

    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private final Admission admission;
    private boolean saved = false;

    private JSpinner dischargeDateSpinner;
    private JTextArea notesArea;

    public AdmissionEditDialog(Frame parent, Admission admission) {
        super(parent, "Edit Admission #" + admission.getAdmissionId(), true);
        this.admission = admission;

        setSize(480, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 99, 235));
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Edit Admission — " + admission.getPatientName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Info Banner ─────────────────────────────────────────────────────
        JPanel info = new JPanel(new GridLayout(1, 3));
        info.setBackground(new Color(239, 246, 255));
        info.setBorder(new EmptyBorder(10, 20, 10, 20));
        info.add(makeInfoLabel("Bed: " + admission.getBedNumber()));
        info.add(makeInfoLabel("Doctor: " + (admission.getDoctorName() != null ? admission.getDoctorName() : "—")));
        info.add(makeInfoLabel("Admitted: " + (admission.getAdmissionDate() != null ? admission.getAdmissionDate().toString().substring(0, 10) : "—")));
        add(info, BorderLayout.AFTER_LAST_LINE);

        // ── Form ────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.weightx = 1.0;

        // Expected Discharge
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(styledLabel("Expected Discharge"), gbc);

        dischargeDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dischargeDateSpinner, "yyyy-MM-dd");
        dischargeDateSpinner.setEditor(dateEditor);
        dischargeDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (admission.getExpectedDischarge() != null) {
            dischargeDateSpinner.setValue(new java.util.Date(admission.getExpectedDischarge().getTime()));
        }
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(dischargeDateSpinner, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        form.add(styledLabel("Notes"), gbc);

        notesArea = new JTextArea(4, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        if (admission.getNotes() != null) notesArea.setText(admission.getNotes());

        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(new JScrollPane(notesArea), gbc);

        // Add info panel at the top of content
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.add(info, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancel = new JButton("Cancel");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton("Save Changes");
        save.setBackground(new Color(37, 99, 235));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> handleSave());

        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
    }

    private void handleSave() {
        Date expectedDischarge = DateUtil.toSqlDate((java.util.Date) dischargeDateSpinner.getValue());
        String notes = notesArea.getText().trim();

        SwingWorkerUtil.execute(
            () -> admissionDAO.updateAdmission(admission.getAdmissionId(), expectedDischarge, notes),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { saved = true; dispose(); }
                    else JOptionPane.showMessageDialog(AdmissionEditDialog.this, "No changes were saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
                @Override public void onError(Exception e) {
                    JOptionPane.showMessageDialog(AdmissionEditDialog.this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        );
    }

    public boolean isSaved() { return saved; }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JLabel makeInfoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(37, 99, 235));
        return l;
    }
}
