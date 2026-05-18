package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.gui.components.ToastNotification;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JToggleButton receptionBtn;
    private JToggleButton adminBtn;

    public LoginFrame() {
        setTitle("Hospital Bed Allocation System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(243, 244, 246));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Logo / Icon placeholder
        JLabel logo = new JLabel("+");
        logo.setFont(new Font("Arial", Font.BOLD, 60));
        logo.setForeground(new Color(37, 99, 235));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logo);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Bed Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(30));

        // Card Panel
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        gbc.gridy = 0; card.add(new JLabel("Select Role"), gbc);
        
        JPanel togglePanel = new JPanel(new GridLayout(1, 2));
        receptionBtn = new JToggleButton("Receptionist", true);
        adminBtn = new JToggleButton("Admin");
        ButtonGroup group = new ButtonGroup();
        group.add(receptionBtn);
        group.add(adminBtn);
        togglePanel.add(receptionBtn);
        togglePanel.add(adminBtn);
        gbc.gridy = 1; card.add(togglePanel, gbc);

        gbc.gridy = 2; card.add(new JLabel("Username"), gbc);
        userField = new JTextField("admin", 15);
        gbc.gridy = 3; card.add(userField, gbc);

        gbc.gridy = 4; card.add(new JLabel("Password"), gbc);
        passField = new JPasswordField("password", 15);
        gbc.gridy = 5; card.add(passField, gbc);

        JButton loginBtn = new JButton("SIGN IN");
        loginBtn.setBackground(new Color(37, 99, 235));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(0, 40));
        loginBtn.addActionListener(this::handleLogin);
        gbc.gridy = 6; 
        gbc.insets = new Insets(20, 0, 0, 0);
        card.add(loginBtn, gbc);

        // Allow pressing Enter to login
        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin(null);
            }
        };
        userField.addActionListener(loginAction);
        passField.addActionListener(loginAction);

        mainPanel.add(card);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void handleLogin(ActionEvent e) {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        String role = receptionBtn.isSelected() ? "Receptionist" : "Admin";

        try {
            // Simplified authentication for demo
            if (user.equals("admin") && pass.equals("password")) {
                MainFrame main = new MainFrame(user, role);
                main.setVisible(true);
                this.dispose();
            } else {
                ToastNotification.showError(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            System.err.println("CRITICAL ERROR DURING LOGIN/INITIALIZATION:");
            ex.printStackTrace();
            ToastNotification.showError(this, "System Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
