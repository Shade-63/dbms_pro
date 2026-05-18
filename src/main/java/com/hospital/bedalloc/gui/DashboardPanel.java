package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.BedDAO;
import com.hospital.bedalloc.gui.components.StatCard;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {
    private MainFrame parent;
    private JPanel bedGrid;
    private JPanel statsPanel;
    private BedDAO bedDAO = new BedDAO();

    public DashboardPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. Stats Panel
        statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        add(statsPanel, BorderLayout.NORTH);

        // 2. Bed Grid
        bedGrid = new JPanel(new GridLayout(0, 4, 20, 20));
        bedGrid.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(bedGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        SwingWorkerUtil.execute(
            () -> bedDAO.getAllBedsWithWard(),
            new SwingWorkerUtil.Callback<List<Bed>>() {
                @Override
                public void onComplete(List<Bed> beds) {
                    updateStats(beds);
                    updateGrid(beds);
                }

                @Override
                public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load beds: " + e.getMessage());
                }
            }
        );
    }

    private void updateStats(List<Bed> beds) {
        statsPanel.removeAll();
        long total = beds.size();
        long available = beds.stream().filter(b -> b.getCurrentStatus().equals("Available")).count();
        long occupied = beds.stream().filter(b -> b.getCurrentStatus().equals("Occupied")).count();
        long maintenance = beds.stream().filter(b -> b.getCurrentStatus().equals("Maintenance")).count();

        statsPanel.add(new StatCard("Total Beds", String.valueOf(total), new Color(107, 114, 128)));
        statsPanel.add(new StatCard("Available", String.valueOf(available), new Color(16, 185, 129)));
        statsPanel.add(new StatCard("Occupied", String.valueOf(occupied), new Color(239, 68, 68)));
        statsPanel.add(new StatCard("Maintenance", String.valueOf(maintenance), new Color(245, 158, 11)));
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void updateGrid(List<Bed> beds) {
        bedGrid.removeAll();
        for (Bed bed : beds) {
            bedGrid.add(createBedCard(bed));
        }
        bedGrid.revalidate();
        bedGrid.repaint();
    }

    private JPanel createBedCard(Bed bed) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Status Strip
        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(0, 4));
        Color statusColor;
        switch (bed.getCurrentStatus()) {
            case "Available": statusColor = new Color(16, 185, 129); break;
            case "Occupied": statusColor = new Color(239, 68, 68); break;
            case "Maintenance": statusColor = new Color(245, 158, 11); break;
            default: statusColor = Color.GRAY;
        }
        strip.setBackground(statusColor);
        card.add(strip, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel numLabel = new JLabel(bed.getBedNumber());
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel wardLabel = new JLabel(bed.getWardType());
        wardLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wardLabel.setForeground(new Color(107, 114, 128));
        info.add(numLabel);
        info.add(wardLabel);
        card.add(info, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel(bed.getCurrentStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(statusColor);
        card.add(statusLabel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (bed.getCurrentStatus().equals("Available")) {
                    new AdmitPatientDialog(parent, bed, DashboardPanel.this).setVisible(true);
                } else if (bed.getCurrentStatus().equals("Occupied")) {
                    // Discharge dialog will be implemented next
                    new DischargeDialog(parent, bed, DashboardPanel.this).setVisible(true);
                }
            }
        });

        return card;
    }
}
