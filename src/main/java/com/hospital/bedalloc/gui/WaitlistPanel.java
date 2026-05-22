package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.WaitlistDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Waitlist;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Waitlist management panel with full CRUD:
 * - Add to Waitlist
 * - Remove Entry
 * - Edit Priority
 * - Process Waitlist (stored procedure)
 */
public class WaitlistPanel extends JPanel {

    private final MainFrame parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private final WaitlistDAO waitlistDAO = new WaitlistDAO();

    private static final String[] COLUMNS = {"ID", "Patient Name", "Ward Requested", "Priority", "Request Time", "Status"};

    public WaitlistPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        refreshData();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Waitlist Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));
        header.add(title, BorderLayout.WEST);

        // Toolbar buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(createButton("＋ Add to Waitlist",  new Color(37, 99, 235),  e -> handleAdd()));
        buttons.add(createButton("✕ Remove Entry",      new Color(239, 68, 68),  e -> handleRemove()));
        buttons.add(createButton("✎ Edit Priority",     new Color(245, 158, 11), e -> handleEditPriority()));
        buttons.add(createButton("⚡ Process Waitlist", new Color(16, 185, 129), e -> handleProcess()));
        buttons.add(createButton("↺ Refresh",           new Color(107,114,128),  e -> refreshData()));
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(229, 231, 235));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(new Color(75, 85, 99));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void refreshData() {
        SwingWorkerUtil.execute(
            () -> waitlistDAO.getActiveWaitlist(),
            new SwingWorkerUtil.Callback<List<Waitlist>>() {
                @Override public void onComplete(List<Waitlist> list) {
                    tableModel.setRowCount(0);
                    for (Waitlist w : list) {
                        tableModel.addRow(new Object[]{
                            w.getWaitlistId(),
                            w.getPatientName(),
                            w.getRequestedWardType(),
                            w.getPriorityScore(),
                            w.getRequestTime(),
                            w.getStatus()
                        });
                    }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load waitlist: " + e.getMessage());
                }
            }
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void handleAdd() {
        WaitlistAddDialog dialog = new WaitlistAddDialog(parent);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            ToastNotification.showSuccess(parent, "Patient added to waitlist.");
        }
    }

    private void handleRemove() {
        int row = table.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a waitlist entry to remove."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int    id    = (int)    tableModel.getValueAt(modelRow, 0);
        String name  = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(parent,
            "<html>Remove <b>" + name + "</b> from the waitlist?</html>",
            "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(
            () -> waitlistDAO.removeFromWaitlist(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { refreshData(); ToastNotification.showSuccess(parent, name + " removed from waitlist."); }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Remove failed: " + e.getMessage());
                }
            }
        );
    }

    private void handleEditPriority() {
        int row = table.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a waitlist entry to edit."); return; }
        int modelRow     = table.convertRowIndexToModel(row);
        int    id        = (int)    tableModel.getValueAt(modelRow, 0);
        String name      = (String) tableModel.getValueAt(modelRow, 1);
        int    curPriority = (int)  tableModel.getValueAt(modelRow, 3);

        SpinnerNumberModel spinModel = new SpinnerNumberModel(curPriority, 1, 100, 1);
        JSpinner spinner = new JSpinner(spinModel);

        int result = JOptionPane.showConfirmDialog(parent,
            new Object[]{"New priority for " + name + ":", spinner},
            "Edit Priority", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;
        int newPriority = (int) spinner.getValue();

        SwingWorkerUtil.execute(
            () -> waitlistDAO.updateWaitlistPriority(id, newPriority),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { refreshData(); ToastNotification.showSuccess(parent, "Priority updated to " + newPriority + "."); }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Update failed: " + e.getMessage());
                }
            }
        );
    }

    private void handleProcess() {
        SwingWorkerUtil.execute(
            () -> { waitlistDAO.processWaitlist(); return null; },
            new SwingWorkerUtil.Callback<Object>() {
                @Override public void onComplete(Object result) {
                    ToastNotification.showSuccess(parent, "Waitlist processing complete.");
                    refreshData();
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Processing failed: " + e.getMessage());
                }
            }
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────
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
