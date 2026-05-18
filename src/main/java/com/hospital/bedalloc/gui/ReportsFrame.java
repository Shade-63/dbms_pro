package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.ReportDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ReportsFrame extends JPanel {
    private MainFrame parent;
    private ReportDAO reportDAO = new ReportDAO();
    private JTabbedPane tabbedPane;

    public ReportsFrame(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Occupancy", createReportPanel("Occupancy"));
        tabbedPane.addTab("Turnover", createReportPanel("Turnover"));
        tabbedPane.addTab("Doctor Workload", createReportPanel("Workload"));
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createReportPanel(String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setRowHeight(30);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> loadReport(type, model));
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        loadReport(type, model);
        return panel;
    }

    private void loadReport(String type, DefaultTableModel model) {
        SwingWorkerUtil.execute(
            () -> {
                switch (type) {
                    case "Occupancy": return reportDAO.getOccupancyReport();
                    case "Turnover": return reportDAO.getTurnoverReport();
                    case "Workload": return reportDAO.getDoctorWorkload();
                    default: return null;
                }
            },
            new SwingWorkerUtil.Callback<List<Map<String, Object>>>() {
                @Override
                public void onComplete(List<Map<String, Object>> data) {
                    if (data == null || data.isEmpty()) return;
                    
                    Vector<String> columns = new Vector<>(data.get(0).keySet());
                    Vector<Vector<Object>> rows = new Vector<>();
                    for (Map<String, Object> map : data) {
                        rows.add(new Vector<>(map.values()));
                    }
                    model.setDataVector(rows, columns);
                }
                @Override
                public void onError(Exception e) {
                    ToastNotification.showError(parent, "Report load failed");
                }
            }
        );
    }
}
