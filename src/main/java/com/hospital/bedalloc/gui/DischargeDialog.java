package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.AdmissionDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Admission;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.service.DischargeService;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DischargeDialog extends JDialog {
    private MainFrame parent;
    private Bed bed;
    private DashboardPanel dashboard;
    private Admission admission;
    private JCheckBox maintCheck;
    private JTextField reasonField;
    private JLabel infoLabel;
    
    private AdmissionDAO admissionDAO = new AdmissionDAO();
    private DischargeService dischargeService = new DischargeService();

    public DischargeDialog(MainFrame parent, Bed bed, DashboardPanel dashboard) {
        super(parent, "Discharge Patient - Bed " + bed.getBedNumber(), true);
        this.parent = parent;
        this.bed = bed;
        this.dashboard = dashboard;

        setSize(450, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);

        infoLabel = new JLabel("Fetching admission details...");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(infoLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        maintCheck = new JCheckBox("Mark bed for maintenance after discharge");
        maintCheck.setOpaque(false);
        mainPanel.add(maintCheck);
        
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JLabel("Maintenance Reason (if any)"));
        reasonField = new JTextField();
        mainPanel.add(reasonField);

        add(mainPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(new Color(249, 250, 251));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        JButton dischargeBtn = new JButton("Confirm Discharge");
        dischargeBtn.setBackground(new Color(239, 68, 68));
        dischargeBtn.setForeground(Color.WHITE);
        dischargeBtn.addActionListener(e -> handleDischarge());
        footer.add(cancelBtn);
        footer.add(dischargeBtn);
        add(footer, BorderLayout.SOUTH);

        loadAdmission();
    }

    private void loadAdmission() {
        SwingWorkerUtil.execute(
            () -> admissionDAO.getActiveAdmissions(),
            new SwingWorkerUtil.Callback<List<Admission>>() {
                @Override
                public void onComplete(List<Admission> admissions) {
                    for (Admission a : admissions) {
                        if (a.getBedId() == bed.getBedId()) {
                            admission = a;
                            infoLabel.setText("<html><b>Patient:</b> " + a.getPatientName() + 
                                            "<br><b>Admitted:</b> " + a.getAdmissionDate() + "</html>");
                            return;
                        }
                    }
                    ToastNotification.showError(parent, "Could not find active admission for this bed");
                    dispose();
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Error loading admission: " + e.getMessage());
                    dispose();
                }
            }
        );
    }

    private void handleDischarge() {
        if (admission == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to discharge " + admission.getPatientName() + "?", 
            "Confirm Discharge", JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(
            () -> dischargeService.dischargePatient(admission.getAdmissionId(), 
                maintCheck.isSelected(), reasonField.getText()),
            new SwingWorkerUtil.Callback<String>() {
                @Override
                public void onComplete(String message) {
                    ToastNotification.showSuccess(parent, message);
                    dashboard.refreshData();
                    dispose();
                }
                @Override
                public void onError(Exception e) {
                    ToastNotification.showError(parent, "Discharge failed: " + e.getMessage());
                }
            }
        );
    }
}
