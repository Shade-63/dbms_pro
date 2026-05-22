package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.PatientDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Patient;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Full CRUD panel for Patient management.
 * Accessible by both Receptionist and Admin roles.
 */
public class PatientManagementPanel extends JPanel {

    private final MainFrame parent;
    private final PatientDAO patientDAO = new PatientDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private static final String[] COLUMNS = {"ID", "Name", "Age", "Blood Group", "Contact", "Emergency Contact", "Registered"};

    public PatientManagementPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        refreshData();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Patient Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));
        header.add(title, BorderLayout.WEST);

        // Search + Buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or contact…");
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        controls.add(searchField);

        controls.add(createButton("＋ Add Patient",  new Color(37, 99, 235),  e -> openAddDialog()));
        controls.add(createButton("✎ Edit",          new Color(16, 185, 129), e -> openEditDialog()));
        controls.add(createButton("✕ Delete",         new Color(239, 68, 68),  e -> handleDelete()));
        controls.add(createButton("↺ Refresh",        new Color(107, 114, 128),e -> refreshData()));

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    // ── Table ────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(229, 231, 235));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(new Color(75, 85, 99));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Hide ID column from display but keep for data retrieval
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Double-click → edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    public void refreshData() {
        SwingWorkerUtil.execute(
            () -> patientDAO.getAllPatients(),
            new SwingWorkerUtil.Callback<List<Patient>>() {
                @Override public void onComplete(List<Patient> list) {
                    tableModel.setRowCount(0);
                    for (Patient p : list) {
                        tableModel.addRow(new Object[]{
                            p.getPatientId(),
                            p.getName(),
                            p.getAge(),
                            p.getBloodGroup(),
                            p.getContact(),
                            p.getEmergencyContact(),
                            p.getRegisteredDate() != null ? p.getRegisteredDate().toString().substring(0, 10) : ""
                        });
                    }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load patients: " + e.getMessage());
                }
            }
        );
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Search across Name (col 1) and Contact (col 4)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 4));
        }
    }

    // ── CRUD Actions ─────────────────────────────────────────────────────────
    private void openAddDialog() {
        PatientFormDialog dialog = new PatientFormDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            ToastNotification.showSuccess(parent, "Patient added successfully.");
        }
    }

    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a patient to edit."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        try {
            Patient p = patientDAO.getPatientById(id);
            if (p == null) { ToastNotification.showError(parent, "Patient not found."); return; }

            PatientFormDialog dialog = new PatientFormDialog(parent, p);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
                ToastNotification.showSuccess(parent, "Patient updated successfully.");
            }
        } catch (Exception ex) {
            ToastNotification.showError(parent, "Error: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a patient to delete."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        String name  = (String) tableModel.getValueAt(modelRow, 1);
        int    id    = (int)    tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(parent,
            "<html>Are you sure you want to delete patient <b>" + name + "</b>?<br>" +
            "This action cannot be undone.</html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(
            () -> patientDAO.deletePatient(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) {
                        refreshData();
                        ToastNotification.showSuccess(parent, "Patient \"" + name + "\" deleted.");
                    }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Delete failed: " + e.getMessage());
                }
            }
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JButton createButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 32));
        btn.addActionListener(action);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
