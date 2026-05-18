package com.hospital.bedalloc.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToastNotification extends JWindow {
    public ToastNotification(JFrame parent, String message, Color color) {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, BorderLayout.CENTER);

        add(panel);
        pack();

        // Position: Top Right
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(scr.width - getWidth() - 20, 50);

        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        
        setVisible(true);
    }

    public static void showSuccess(JFrame parent, String msg) {
        new ToastNotification(parent, msg, new Color(16, 185, 129));
    }

    public static void showError(JFrame parent, String msg) {
        new ToastNotification(parent, msg, new Color(239, 68, 68));
    }
}
