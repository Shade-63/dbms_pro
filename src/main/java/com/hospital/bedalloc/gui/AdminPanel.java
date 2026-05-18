package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.WardDAO;
import com.hospital.bedalloc.dao.StaffDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Ward;
import com.hospital.bedalloc.model.Staff;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private MainFrame parent;
    private WardDAO wardDAO = new WardDAO();
    private StaffDAO staffDAO = new StaffDAO();

    public AdminPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Administration Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Wards", createWardPanel());
        tabs.addTab("Staff", createStaffPanel());
        
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createWardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Type", "Floor", "Capacity", "Charge"}, 0);
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        SwingWorkerUtil.execute(
            () -> wardDAO.getAllWards(),
            new SwingWorkerUtil.Callback<List<Ward>>() {
                @Override
                public void onComplete(List<Ward> list) {
                    for (Ward w : list) model.addRow(new Object[]{w.getWardId(), w.getWardType(), w.getFloor(), w.getCapacity(), w.getDailyCharge()});
                }
                @Override public void onError(Exception e) {}
            }
        );
        return p;
    }

    private JPanel createStaffPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Role", "Dept", "Phone"}, 0);
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        SwingWorkerUtil.execute(
            () -> staffDAO.getDoctors(), // Simplified: only doctors for now
            new SwingWorkerUtil.Callback<List<Staff>>() {
                @Override
                public void onComplete(List<Staff> list) {
                    for (Staff s : list) model.addRow(new Object[]{s.getStaffId(), s.getName(), s.getRole(), s.getDepartment(), s.getPhone()});
                }
                @Override public void onError(Exception e) {}
            }
        );
        return p;
    }
}
