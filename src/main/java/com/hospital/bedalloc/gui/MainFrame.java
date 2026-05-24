package com.hospital.bedalloc.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private String username;
    private String role;
    private JPanel contentArea;
    private JLabel statusLabel;
    private JButton activeNavBtn = null;

    public MainFrame(String username, String role) {
        this.username = username;
        this.role = role;

        setTitle("Hospital Bed Allocation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── 1. Top Bar ───────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(37, 99, 235));
        topBar.setPreferredSize(new Dimension(0, 58));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel brand = new JLabel("🏥  HOSPITAL BED ALLOCATION SYSTEM");
        brand.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        brand.setForeground(Color.WHITE);
        topBar.add(brand, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel(username + "  |  " + role);
        userLabel.setForeground(new Color(186, 230, 253));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userPanel.add(userLabel);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setBackground(new Color(30, 64, 175));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        userPanel.add(logoutBtn);
        topBar.add(userPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── 2. Sidebar ───────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(17, 24, 39));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Sidebar brand label
        JLabel sideTitle = new JLabel("  Navigation");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sideTitle.setForeground(new Color(107, 114, 128));
        sideTitle.setMaximumSize(new Dimension(220, 40));
        sideTitle.setPreferredSize(new Dimension(220, 40));
        sideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideTitle.setBorder(BorderFactory.createEmptyBorder(18, 16, 5, 0));
        sidebar.add(sideTitle);

        // Menu items — icon + label, role-based visibility
        addNavItem(sidebar, "📊  Dashboard",    "Dashboard",   true);
        addNavItem(sidebar, "👥  Patients",     "Patients",    true);
        addNavItem(sidebar, "🛏  Admissions",   "Admissions",  true);
        addNavItem(sidebar, "⏳  Waitlist",     "Waitlist",    true);
        addNavItem(sidebar, "📈  Reports",      "Reports",     true);
        addNavItem(sidebar, "⚙️  Admin Panel",  "Admin Panel", role.equals("Admin"));

        sidebar.add(Box.createVerticalGlue());
        add(sidebar, BorderLayout.WEST);

        // ── 3. Content Area ───────────────────────────────────────────────────
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(249, 250, 251));
        add(contentArea, BorderLayout.CENTER);

        // ── 4. Status Bar ─────────────────────────────────────────────────────
        statusLabel = new JLabel("  Ready");
        statusLabel.setPreferredSize(new Dimension(0, 24));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(107, 114, 128));
        statusLabel.setBackground(new Color(243, 244, 246));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));
        add(statusLabel, BorderLayout.SOUTH);

        // Default panel
        switchPanel("Dashboard");
    }

    private void addNavItem(JPanel sidebar, String label, String panelName, boolean visible) {
        if (!visible) return;

        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(220, 46));
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setMinimumSize(new Dimension(220, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(new Color(17, 24, 39));
        btn.setForeground(new Color(209, 213, 219));
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(new Color(31, 41, 55));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(new Color(17, 24, 39));
            }
        });

        btn.addActionListener(e -> {
            setActiveNav(btn);
            switchPanel(panelName);
        });

        sidebar.add(btn);
    }

    private void setActiveNav(JButton btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(new Color(17, 24, 39));
            activeNavBtn.setForeground(new Color(209, 213, 219));
        }
        activeNavBtn = btn;
        btn.setBackground(new Color(37, 99, 235));
        btn.setForeground(Color.WHITE);
    }

    private void switchPanel(String panelName) {
        contentArea.removeAll();
        statusLabel.setText("  Loading " + panelName + "…");

        switch (panelName) {
            case "Dashboard":
                contentArea.add(new DashboardPanel(this), BorderLayout.CENTER);
                break;
            case "Patients":
                contentArea.add(new PatientManagementPanel(this), BorderLayout.CENTER);
                break;
            case "Admissions":
                contentArea.add(new AdmissionsPanel(this), BorderLayout.CENTER);
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
        statusLabel.setText("  " + panelName);
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
}
