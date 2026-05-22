package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.BedDAO;
import com.hospital.bedalloc.dao.StaffDAO;
import com.hospital.bedalloc.dao.WardDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.model.Staff;
import com.hospital.bedalloc.model.Ward;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Administration Panel — tabs for Wards, Beds, Staff.
 * All tabs support full CRUD operations.
 */
public class AdminPanel extends JPanel {

    private final MainFrame parent;
    private final WardDAO wardDAO = new WardDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    // Ward tab
    private JTable wardTable;
    private DefaultTableModel wardModel;

    // Bed tab
    private JTable bedTable;
    private DefaultTableModel bedModel;

    // Staff tab
    private JTable staffTable;
    private DefaultTableModel staffModel;

    public AdminPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Administration Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("🏥 Wards",  buildWardTab());
        tabs.addTab("🛏 Beds",   buildBedTab());
        tabs.addTab("👤 Staff",  buildStaffTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════════
    // WARDS TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildWardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel toolbar = buildToolbar(
            new ToolbarAction("＋ Add Ward",   new Color(37, 99, 235),  e -> openWardAdd()),
            new ToolbarAction("✎ Edit Ward",   new Color(16, 185, 129), e -> openWardEdit()),
            new ToolbarAction("✕ Delete Ward", new Color(239, 68, 68),  e -> deleteWard()),
            new ToolbarAction("↺ Refresh",     new Color(107,114,128),  e -> loadWards())
        );
        panel.add(toolbar, BorderLayout.NORTH);

        wardModel = new DefaultTableModel(new String[]{"ID","Ward Type","Floor","Capacity","Daily Charge (₹)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        wardTable = buildStyledTable(wardModel);
        wardTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openWardEdit(); }
        });
        hideIdColumn(wardTable);

        panel.add(new JScrollPane(wardTable), BorderLayout.CENTER);
        loadWards();
        return panel;
    }

    private void loadWards() {
        SwingWorkerUtil.execute(() -> wardDAO.getAllWards(),
            new SwingWorkerUtil.Callback<List<Ward>>() {
                @Override public void onComplete(List<Ward> list) {
                    wardModel.setRowCount(0);
                    for (Ward w : list)
                        wardModel.addRow(new Object[]{w.getWardId(), w.getWardType(), w.getFloor(), w.getCapacity(), String.format("%.2f", w.getDailyCharge())});
                }
                @Override public void onError(Exception e) { ToastNotification.showError(parent, "Failed to load wards: " + e.getMessage()); }
            });
    }

    private void openWardAdd() {
        WardFormDialog dialog = new WardFormDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) { loadWards(); ToastNotification.showSuccess(parent, "Ward added successfully."); }
    }

    private void openWardEdit() {
        int row = wardTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a ward to edit."); return; }
        int id = (int) wardModel.getValueAt(wardTable.convertRowIndexToModel(row), 0);
        try {
            Ward w = wardDAO.getWardById(id);
            WardFormDialog dialog = new WardFormDialog(parent, w);
            dialog.setVisible(true);
            if (dialog.isSaved()) { loadWards(); ToastNotification.showSuccess(parent, "Ward updated."); }
        } catch (Exception ex) { ToastNotification.showError(parent, "Error: " + ex.getMessage()); }
    }

    private void deleteWard() {
        int row = wardTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a ward to delete."); return; }
        int modelRow = wardTable.convertRowIndexToModel(row);
        int    id   = (int)    wardModel.getValueAt(modelRow, 0);
        String type = (String) wardModel.getValueAt(modelRow, 1);

        if (JOptionPane.showConfirmDialog(parent,
                "<html>Delete ward <b>" + type + "</b>?<br>All beds in this ward must be removed first.</html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(() -> wardDAO.deleteWard(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) { if (ok) { loadWards(); ToastNotification.showSuccess(parent, "Ward deleted."); } }
                @Override public void onError(Exception e)  { ToastNotification.showError(parent, "Delete failed: " + e.getMessage()); }
            });
    }

    // ════════════════════════════════════════════════════════════════════════
    // BEDS TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildBedTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel toolbar = buildToolbar(
            new ToolbarAction("＋ Add Bed",       new Color(37, 99, 235),  e -> openBedAdd()),
            new ToolbarAction("✎ Edit Bed",       new Color(16, 185, 129), e -> openBedEdit()),
            new ToolbarAction("✕ Delete Bed",     new Color(239, 68, 68),  e -> deleteBed()),
            new ToolbarAction("🔄 Change Status", new Color(245, 158, 11), e -> changeBedStatus()),
            new ToolbarAction("↺ Refresh",        new Color(107,114,128),  e -> loadBeds())
        );
        panel.add(toolbar, BorderLayout.NORTH);

        bedModel = new DefaultTableModel(new String[]{"ID","Bed No.","Ward","Equipment Status","Current Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bedTable = buildStyledTable(bedModel);
        bedTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openBedEdit(); }
        });
        hideIdColumn(bedTable);

        panel.add(new JScrollPane(bedTable), BorderLayout.CENTER);
        loadBeds();
        return panel;
    }

    private void loadBeds() {
        SwingWorkerUtil.execute(() -> bedDAO.getAllBedsWithWard(),
            new SwingWorkerUtil.Callback<List<Bed>>() {
                @Override public void onComplete(List<Bed> list) {
                    bedModel.setRowCount(0);
                    for (Bed b : list)
                        bedModel.addRow(new Object[]{b.getBedId(), b.getBedNumber(), b.getWardType(), b.getEquipmentStatus(), b.getCurrentStatus()});
                }
                @Override public void onError(Exception e) { ToastNotification.showError(parent, "Failed to load beds: " + e.getMessage()); }
            });
    }

    private void openBedAdd() {
        BedFormDialog dialog = new BedFormDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) { loadBeds(); ToastNotification.showSuccess(parent, "Bed added successfully."); }
    }

    private void openBedEdit() {
        int row = bedTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a bed to edit."); return; }
        int id = (int) bedModel.getValueAt(bedTable.convertRowIndexToModel(row), 0);
        try {
            Bed b = bedDAO.getBedById(id);
            BedFormDialog dialog = new BedFormDialog(parent, b);
            dialog.setVisible(true);
            if (dialog.isSaved()) { loadBeds(); ToastNotification.showSuccess(parent, "Bed updated."); }
        } catch (Exception ex) { ToastNotification.showError(parent, "Error: " + ex.getMessage()); }
    }

    private void deleteBed() {
        int row = bedTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a bed to delete."); return; }
        int modelRow = bedTable.convertRowIndexToModel(row);
        int    id    = (int)    bedModel.getValueAt(modelRow, 0);
        String bedNo = (String) bedModel.getValueAt(modelRow, 1);

        if (JOptionPane.showConfirmDialog(parent,
                "<html>Delete bed <b>" + bedNo + "</b>? This cannot be undone.</html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(() -> bedDAO.deleteBed(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) { if (ok) { loadBeds(); ToastNotification.showSuccess(parent, "Bed " + bedNo + " deleted."); } }
                @Override public void onError(Exception e)  { ToastNotification.showError(parent, "Delete failed: " + e.getMessage()); }
            });
    }

    private void changeBedStatus() {
        int row = bedTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a bed to change status."); return; }
        int modelRow  = bedTable.convertRowIndexToModel(row);
        int    id     = (int)    bedModel.getValueAt(modelRow, 0);
        String bedNo  = (String) bedModel.getValueAt(modelRow, 1);
        String curSt  = (String) bedModel.getValueAt(modelRow, 4);

        if ("Occupied".equals(curSt)) { ToastNotification.showError(parent, "Cannot change status of an occupied bed."); return; }

        String[] options = {"Available", "Maintenance"};
        String chosen = (String) JOptionPane.showInputDialog(parent,
            "Select new status for bed " + bedNo + ":", "Change Bed Status",
            JOptionPane.PLAIN_MESSAGE, null, options, curSt);
        if (chosen == null || chosen.equals(curSt)) return;

        SwingWorkerUtil.execute(() -> bedDAO.updateBedStatus(id, chosen),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) { if (ok) { loadBeds(); ToastNotification.showSuccess(parent, "Bed " + bedNo + " → " + chosen); } }
                @Override public void onError(Exception e)  { ToastNotification.showError(parent, "Status change failed: " + e.getMessage()); }
            });
    }

    // ════════════════════════════════════════════════════════════════════════
    // STAFF TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildStaffTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel toolbar = buildToolbar(
            new ToolbarAction("＋ Add Staff",   new Color(37, 99, 235),  e -> openStaffAdd()),
            new ToolbarAction("✎ Edit Staff",   new Color(16, 185, 129), e -> openStaffEdit()),
            new ToolbarAction("✕ Delete Staff", new Color(239, 68, 68),  e -> deleteStaff()),
            new ToolbarAction("↺ Refresh",      new Color(107,114,128),  e -> loadStaff())
        );
        panel.add(toolbar, BorderLayout.NORTH);

        staffModel = new DefaultTableModel(new String[]{"ID","Name","Role","Department","Phone"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = buildStyledTable(staffModel);
        staffTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openStaffEdit(); }
        });
        hideIdColumn(staffTable);

        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        loadStaff();
        return panel;
    }

    private void loadStaff() {
        SwingWorkerUtil.execute(() -> staffDAO.getAllStaff(),
            new SwingWorkerUtil.Callback<List<Staff>>() {
                @Override public void onComplete(List<Staff> list) {
                    staffModel.setRowCount(0);
                    for (Staff s : list)
                        staffModel.addRow(new Object[]{s.getStaffId(), s.getName(), s.getRole(), s.getDepartment(), s.getPhone()});
                }
                @Override public void onError(Exception e) { ToastNotification.showError(parent, "Failed to load staff: " + e.getMessage()); }
            });
    }

    private void openStaffAdd() {
        StaffFormDialog dialog = new StaffFormDialog(parent, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) { loadStaff(); ToastNotification.showSuccess(parent, "Staff member added."); }
    }

    private void openStaffEdit() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a staff member to edit."); return; }
        int id = (int) staffModel.getValueAt(staffTable.convertRowIndexToModel(row), 0);
        try {
            Staff s = staffDAO.getStaffById(id);
            StaffFormDialog dialog = new StaffFormDialog(parent, s);
            dialog.setVisible(true);
            if (dialog.isSaved()) { loadStaff(); ToastNotification.showSuccess(parent, "Staff member updated."); }
        } catch (Exception ex) { ToastNotification.showError(parent, "Error: " + ex.getMessage()); }
    }

    private void deleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a staff member to delete."); return; }
        int modelRow = staffTable.convertRowIndexToModel(row);
        int    id   = (int)    staffModel.getValueAt(modelRow, 0);
        String name = (String) staffModel.getValueAt(modelRow, 1);

        if (JOptionPane.showConfirmDialog(parent,
                "<html>Delete staff member <b>" + name + "</b>?</html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(() -> staffDAO.deleteStaff(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) { if (ok) { loadStaff(); ToastNotification.showSuccess(parent, name + " removed."); } }
                @Override public void onError(Exception e)  { ToastNotification.showError(parent, "Delete failed: " + e.getMessage()); }
            });
    }

    // ── Shared Helpers ────────────────────────────────────────────────────────
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

    private JPanel buildToolbar(ToolbarAction... actions) {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tb.setBackground(new Color(249, 250, 251));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        for (ToolbarAction a : actions) {
            JButton btn = new JButton(a.label);
            btn.setBackground(a.color);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.addActionListener(a.action);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tb.add(btn);
        }
        return tb;
    }

    private static class ToolbarAction {
        String label; Color color; java.awt.event.ActionListener action;
        ToolbarAction(String l, Color c, java.awt.event.ActionListener a) { label = l; color = c; action = a; }
    }
}
