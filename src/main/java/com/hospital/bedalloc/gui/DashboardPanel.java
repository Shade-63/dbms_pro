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
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private final MainFrame parent;
    private JPanel bedGrid;
    private JPanel statsPanel;
    private JComboBox<String> wardFilter;
    private JComboBox<String> statusFilter;
    private JLabel bedCountLabel;
    private final BedDAO bedDAO = new BedDAO();

    private List<Bed> allBeds = List.of(); // cached full list

    public DashboardPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // ── Stats row ───────────────────────────────────────────────────────
        statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));

        // ── Filter bar ──────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterBar.setOpaque(false);

        JLabel title = new JLabel("Bed Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(17, 24, 39));
        filterBar.add(title);

        filterBar.add(Box.createHorizontalStrut(20));
        filterBar.add(styledLabel("Ward:"));
        wardFilter = new JComboBox<>(new String[]{"All Wards","General","ICU","Emergency","Pediatric","Maternity","Private","Semi-Private"});
        wardFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        wardFilter.setPreferredSize(new Dimension(160, 30));
        wardFilter.addActionListener(e -> applyFilter());
        filterBar.add(wardFilter);

        filterBar.add(styledLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All Status","Available","Occupied","Maintenance"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(150, 30));
        statusFilter.addActionListener(e -> applyFilter());
        filterBar.add(statusFilter);

        bedCountLabel = new JLabel("");
        bedCountLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        bedCountLabel.setForeground(new Color(107, 114, 128));
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(bedCountLabel);

        JButton refreshBtn = new JButton("↺ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(107, 114, 128));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(refreshBtn);

        // North: stats + filter
        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(statsPanel, BorderLayout.NORTH);
        northPanel.add(filterBar,  BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // ── Bed Grid ─────────────────────────────────────────────────────────
        bedGrid = new JPanel(new GridLayout(0, 4, 18, 18));
        bedGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(bedGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        SwingWorkerUtil.execute(
            () -> bedDAO.getAllBedsWithWard(),
            new SwingWorkerUtil.Callback<List<Bed>>() {
                @Override public void onComplete(List<Bed> beds) {
                    allBeds = beds;
                    updateStats(beds);
                    applyFilter();
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load beds: " + e.getMessage());
                }
            }
        );
    }

    private void applyFilter() {
        String wardSel   = (String) wardFilter.getSelectedItem();
        String statusSel = (String) statusFilter.getSelectedItem();

        List<Bed> filtered = allBeds.stream()
            .filter(b -> "All Wards".equals(wardSel)   || wardSel.equalsIgnoreCase(b.getWardType()))
            .filter(b -> "All Status".equals(statusSel) || statusSel.equals(b.getCurrentStatus()))
            .collect(Collectors.toList());

        bedCountLabel.setText("Showing " + filtered.size() + " of " + allBeds.size() + " beds");
        updateGrid(filtered);
    }

    private void updateStats(List<Bed> beds) {
        statsPanel.removeAll();
        long total       = beds.size();
        long available   = beds.stream().filter(b -> "Available".equals(b.getCurrentStatus())).count();
        long occupied    = beds.stream().filter(b -> "Occupied".equals(b.getCurrentStatus())).count();
        long maintenance = beds.stream().filter(b -> "Maintenance".equals(b.getCurrentStatus())).count();

        statsPanel.add(new StatCard("Total Beds",  String.valueOf(total),       new Color(107, 114, 128)));
        statsPanel.add(new StatCard("Available",   String.valueOf(available),   new Color(16,  185, 129)));
        statsPanel.add(new StatCard("Occupied",    String.valueOf(occupied),    new Color(239, 68,  68)));
        statsPanel.add(new StatCard("Maintenance", String.valueOf(maintenance), new Color(245, 158, 11)));

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void updateGrid(List<Bed> beds) {
        bedGrid.removeAll();
        if (beds.isEmpty()) {
            JLabel empty = new JLabel("No beds match the selected filters.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(new Color(156, 163, 175));
            bedGrid.setLayout(new BorderLayout());
            bedGrid.add(empty, BorderLayout.CENTER);
        } else {
            bedGrid.setLayout(new GridLayout(0, 4, 18, 18));
            for (Bed bed : beds) bedGrid.add(createBedCard(bed));
        }
        bedGrid.revalidate();
        bedGrid.repaint();
    }

    private JPanel createBedCard(Bed bed) {
        Color statusColor;
        switch (bed.getCurrentStatus()) {
            case "Available":   statusColor = new Color(16,  185, 129); break;
            case "Occupied":    statusColor = new Color(239, 68,  68);  break;
            case "Maintenance": statusColor = new Color(245, 158, 11);  break;
            default:            statusColor = Color.GRAY;
        }

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(0, 0, 14, 0)
        ));
        card.setCursor(new Cursor("Occupied".equals(bed.getCurrentStatus()) || "Available".equals(bed.getCurrentStatus())
            ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

        // Top colour strip
        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(0, 5));
        strip.setBackground(statusColor);
        card.add(strip, BorderLayout.NORTH);

        // Centre info
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(8, 14, 0, 14));

        JLabel numLabel = new JLabel(bed.getBedNumber());
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        numLabel.setForeground(new Color(17, 24, 39));

        JLabel wardLabel = new JLabel(bed.getWardType());
        wardLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wardLabel.setForeground(new Color(107, 114, 128));

        JLabel statusLabel = new JLabel(bed.getCurrentStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(statusColor);

        info.add(numLabel);
        info.add(wardLabel);
        info.add(statusLabel);
        card.add(info, BorderLayout.CENTER);

        // Tooltip with action hint
        String tooltip;
        switch (bed.getCurrentStatus()) {
            case "Available":   tooltip = "Click to admit a patient"; break;
            case "Occupied":    tooltip = "Click to discharge patient"; break;
            case "Maintenance": tooltip = "Click to mark as Available"; break;
            default:            tooltip = bed.getBedNumber();
        }
        card.setToolTipText(tooltip);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(statusColor, 2),
                    new EmptyBorder(0, 0, 14, 0)
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                    new EmptyBorder(0, 0, 14, 0)
                ));
            }
            @Override public void mouseClicked(MouseEvent e) {
                switch (bed.getCurrentStatus()) {
                    case "Available":
                        new AdmitPatientDialog(parent, bed, DashboardPanel.this).setVisible(true);
                        break;
                    case "Occupied":
                        new DischargeDialog(parent, bed, DashboardPanel.this).setVisible(true);
                        break;
                    case "Maintenance":
                        int choice = JOptionPane.showConfirmDialog(parent,
                            "Mark bed " + bed.getBedNumber() + " as Available?",
                            "Clear Maintenance", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            try {
                                bedDAO.updateBedStatus(bed.getBedId(), "Available");
                                refreshData();
                                ToastNotification.showSuccess(parent, "Bed " + bed.getBedNumber() + " is now Available.");
                            } catch (Exception ex) {
                                ToastNotification.showError(parent, "Error: " + ex.getMessage());
                            }
                        }
                        break;
                }
            }
        });

        return card;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(55, 65, 81));
        return l;
    }
}
