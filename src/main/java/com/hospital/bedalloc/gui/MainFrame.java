package com.hospital.bedalloc.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private String username;
    private String role;
    private JPanel contentArea;
    private JLabel statusLabel;

    public MainFrame(String username, String role) {
        this.username = username;
        this.role = role;

        setTitle("Hospital Bed Allocation System - " + role);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        
        JLabel brand = new JLabel("  BED ALLOCATION SYSTEM");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(new Color(37, 99, 235));
        topBar.add(brand, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        userPanel.setOpaque(false);
        userPanel.add(new JLabel(username + " (" + role + ")"));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        userPanel.add(logoutBtn);
        topBar.add(userPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        String[] menuItems = {"Dashboard", "Waitlist", "Reports", "Admin Panel"};
        for (String item : menuItems) {
            if (item.equals("Admin Panel") && !role.equals("Admin")) continue;
            
            JButton btn = new JButton(item);
            btn.setMaximumSize(new Dimension(220, 50));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.addActionListener(e -> switchPanel(item));
            sidebar.add(btn);
        }
        add(sidebar, BorderLayout.WEST);

        // 3. Content Area
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(249, 250, 251));
        add(contentArea, BorderLayout.CENTER);

        // 4. Status Bar
        statusLabel = new JLabel("  Ready");
        statusLabel.setPreferredSize(new Dimension(0, 25));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        add(statusLabel, BorderLayout.SOUTH);

        // Default panel
        switchPanel("Dashboard");
    }

    private void switchPanel(String panelName) {
        contentArea.removeAll();
        statusLabel.setText("  Loading " + panelName + "...");
        
        switch (panelName) {
            case "Dashboard":
                contentArea.add(new DashboardPanel(this), BorderLayout.CENTER);
                break;
            case "Waitlist":
                contentArea.add(new WaitlistPanel(this), BorderLayout.CENTER);
                break;
            case "Reports":
                contentArea.add(new ReportsFrame(this), BorderLayout.CENTER);
                break;
            case "Admin Panel":
                contentArea.add(new AdminPanel(this), BorderLayout.CENTER);
                break;
        }
        
        contentArea.revalidate();
        contentArea.repaint();
        statusLabel.setText("  " + panelName + " Ready");
    }
}
