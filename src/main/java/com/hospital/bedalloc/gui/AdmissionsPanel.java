package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.AdmissionDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Admission;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Two-tab panel for managing Admissions:
 * Tab 1 — Active Admissions (Edit + Discharge actions)
 * Tab 2 — Admission History (read-only)
 */
public class AdmissionsPanel extends JPanel {

    private final MainFrame parent;
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    private JTable activeTable, historyTable;
    private DefaultTableModel activeModel, historyModel;

    private static final String[] ACTIVE_COLS  = {"ID","Patient","Bed","Doctor","Admitted","Exp. Discharge","Notes"};
    private static final String[] HISTORY_COLS = {"ID","Patient","Bed","Doctor","Admitted","Discharged","Status"};

    public AdmissionsPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Page Title
        JLabel title = new JLabel("Admissions Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        add(title, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("Active Admissions", buildActiveTab());
        tabs.addTab("Admission History", buildHistoryTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) loadHistory();
        });
        add(tabs, BorderLayout.CENTER);

        loadActive();
    }

    // ── Active Tab ───────────────────────────────────────────────────────────
    private JPanel buildActiveTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(new Color(249, 250, 251));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        toolbar.add(createButton("✎ Edit Admission",   new Color(37, 99, 235),  e -> handleEditAdmission()));
        toolbar.add(createButton("⬆ Discharge Patient", new Color(239, 68, 68),  e -> handleDischarge()));
        toolbar.add(createButton("↺ Refresh",           new Color(107, 114, 128),e -> loadActive()));

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        activeModel = new DefaultTableModel(ACTIVE_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeTable = buildStyledTable(activeModel);
        // Hide ID column
        hideIdColumn(activeTable);

        // Double-click to edit
        activeTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEditAdmission();
            }
        });

        JScrollPane scroll = new JScrollPane(activeTable);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── History Tab ──────────────────────────────────────────────────────────
    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(new Color(249, 250, 251));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        toolbar.add(createButton("↺ Refresh", new Color(107, 114, 128), e -> loadHistory()));
        panel.add(toolbar, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(HISTORY_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = buildStyledTable(historyModel);
        hideIdColumn(historyTable);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Data Loading ──────────────────────────────────────────────────────────
    private void loadActive() {
        SwingWorkerUtil.execute(
            () -> admissionDAO.getActiveAdmissions(),
            new SwingWorkerUtil.Callback<List<Admission>>() {
                @Override public void onComplete(List<Admission> list) {
                    activeModel.setRowCount(0);
                    for (Admission a : list) {
                        activeModel.addRow(new Object[]{
                            a.getAdmissionId(),
                            a.getPatientName(),
                            a.getBedNumber(),
                            a.getDoctorName() != null ? a.getDoctorName() : "—",
                            a.getAdmissionDate() != null ? a.getAdmissionDate().toString().substring(0, 10) : "",
                            a.getExpectedDischarge() != null ? a.getExpectedDischarge().toString() : "—",
                            a.getNotes() != null ? a.getNotes() : ""
                        });
                    }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load admissions: " + e.getMessage());
                }
            }
        );
    }

    private void loadHistory() {
        SwingWorkerUtil.execute(
            () -> admissionDAO.getAdmissionHistory(),
            new SwingWorkerUtil.Callback<List<Admission>>() {
                @Override public void onComplete(List<Admission> list) {
                    historyModel.setRowCount(0);
                    for (Admission a : list) {
                        historyModel.addRow(new Object[]{
                            a.getAdmissionId(),
                            a.getPatientName(),
                            a.getBedNumber(),
                            a.getDoctorName() != null ? a.getDoctorName() : "—",
                            a.getAdmissionDate() != null ? a.getAdmissionDate().toString().substring(0, 10) : "",
                            a.getActualDischarge() != null ? a.getActualDischarge().toString().substring(0, 10) : "—",
                            a.getStatus()
                        });
                    }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load history: " + e.getMessage());
                }
            }
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void handleEditAdmission() {
        int row = activeTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select an active admission to edit."); return; }

        Admission a = buildAdmissionFromRow(row);
        AdmissionEditDialog dialog = new AdmissionEditDialog(parent, a);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadActive();
            ToastNotification.showSuccess(parent, "Admission updated successfully.");
        }
    }

    private void handleDischarge() {
        int row = activeTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select an active admission to discharge."); return; }

        Admission a = buildAdmissionFromRow(row);
        Bed fakeBed = new Bed();
        fakeBed.setBedId(0); // Will be resolved by DischargeDialog via admission lookup
        fakeBed.setBedNumber(a.getBedNumber());
        fakeBed.setCurrentStatus("Occupied");

        // Build a minimal Bed from the row for the discharge dialog
        Bed bed = new Bed(a.getBedId(), 0, a.getBedNumber(), "Standard", "Occupied");

        // Use a lightweight discharge flow using admission directly
        int confirm = JOptionPane.showConfirmDialog(parent,
            "<html>Discharge <b>" + a.getPatientName() + "</b> from Bed <b>" + a.getBedNumber() + "</b>?<br>" +
            "Mark bed for maintenance afterwards?</html>",
            "Confirm Discharge", JOptionPane.YES_NO_CANCEL_OPTION);

        if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) return;
        boolean maintenance = (confirm == JOptionPane.YES_OPTION);

        String reason = "";
        if (maintenance) {
            reason = JOptionPane.showInputDialog(parent, "Enter maintenance reason:", "Maintenance Reason", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) reason = "";
        }

        final String finalReason = reason;
        SwingWorkerUtil.execute(
            () -> admissionDAO.dischargePatient(a.getAdmissionId(), maintenance, finalReason),
            new SwingWorkerUtil.Callback<String>() {
                @Override public void onComplete(String msg) {
                    loadActive();
                    ToastNotification.showSuccess(parent, msg != null ? msg : "Patient discharged successfully.");
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Discharge failed: " + e.getMessage());
                }
            }
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Admission buildAdmissionFromRow(int viewRow) {
        int modelRow = activeTable.convertRowIndexToModel(viewRow);
        Admission a = new Admission();
        a.setAdmissionId((int) activeModel.getValueAt(modelRow, 0));
        a.setPatientName((String) activeModel.getValueAt(modelRow, 1));
        a.setBedNumber((String) activeModel.getValueAt(modelRow, 2));
        a.setDoctorName((String) activeModel.getValueAt(modelRow, 3));
        a.setNotes((String) activeModel.getValueAt(modelRow, 6));
        return a;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(36);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setGridColor(new Color(229, 231, 235));
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(243, 244, 246));
        t.getTableHeader().setForeground(new Color(75, 85, 99));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        return t;
    }

    private void hideIdColumn(JTable t) {
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setWidth(0);
    }

    private JButton createButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.addActionListener(action);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
