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

public class WaitlistPanel extends JPanel {
    private MainFrame parent;
    private JTable table;
    private DefaultTableModel tableModel;
    private WaitlistDAO waitlistDAO = new WaitlistDAO();

    public WaitlistPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Waitlist Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JButton processBtn = new JButton("Process Waitlist");
        processBtn.setBackground(new Color(37, 99, 235));
        processBtn.setForeground(Color.WHITE);
        processBtn.addActionListener(e -> handleProcess());
        header.add(processBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient Name", "Ward Requested", "Priority", "Request Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshData();
    }

    private void refreshData() {
        SwingWorkerUtil.execute(
            () -> waitlistDAO.getActiveWaitlist(),
            new SwingWorkerUtil.Callback<List<Waitlist>>() {
                @Override
                public void onComplete(List<Waitlist> list) {
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
                    ToastNotification.showError(parent, "Failed to load waitlist");
                }
            }
        );
    }

    private void handleProcess() {
        SwingWorkerUtil.execute(
            () -> {
                waitlistDAO.processWaitlist();
                return null;
            },
            new SwingWorkerUtil.Callback<Object>() {
                @Override
                public void onComplete(Object result) {
                    ToastNotification.showSuccess(parent, "Waitlist processing complete");
                    refreshData();
                }
                @Override
                public void onError(Exception e) {
                    ToastNotification.showError(parent, "Processing failed: " + e.getMessage());
                }
            }
        );
    }
}
