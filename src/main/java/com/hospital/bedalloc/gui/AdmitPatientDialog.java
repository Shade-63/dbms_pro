package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.AdmissionDAO;
import com.hospital.bedalloc.dao.StaffDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.model.Patient;
import com.hospital.bedalloc.model.Staff;
import com.hospital.bedalloc.service.BedAllocationService;
import com.hospital.bedalloc.util.DateUtil;
import com.hospital.bedalloc.util.SwingWorkerUtil;
import com.hospital.bedalloc.util.ValidationUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class AdmitPatientDialog extends JDialog {
    private MainFrame parent;
    private Bed bed;
    private DashboardPanel dashboard;
    private JTextField nameField, ageField, contactField, addressField, emContactField;
    private JComboBox<Staff> doctorCombo;
    private JComboBox<String> bloodGroupCombo;
    private JSpinner expectedDischargeSpinner;
    private JTextArea notesArea;
    
    private StaffDAO staffDAO = new StaffDAO();
    private BedAllocationService allocationService = new BedAllocationService();

    public AdmitPatientDialog(MainFrame parent, Bed bed, DashboardPanel dashboard) {
        super(parent, "Admit Patient - Bed " + bed.getBedNumber(), true);
        this.parent = parent;
        this.bed = bed;
        this.dashboard = dashboard;

        setSize(600, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeader("Patient Information"));
        JPanel patientPanel = new JPanel(new GridLayout(0, 2, 20, 15));
        patientPanel.setOpaque(false);
        nameField = addField(patientPanel, "Full Name");
        ageField = addField(patientPanel, "Age");
        contactField = addField(patientPanel, "Contact");
        emContactField = addField(patientPanel, "Emergency Contact");
        
        bloodGroupCombo = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        patientPanel.add(new JLabel("Blood Group"));
        patientPanel.add(bloodGroupCombo);
        
        addressField = addField(patientPanel, "Address");
        mainPanel.add(patientPanel);

        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(createHeader("Admission Details"));
        
        JPanel admissionPanel = new JPanel(new GridLayout(0, 2, 20, 15));
        admissionPanel.setOpaque(false);
        admissionPanel.add(new JLabel("Ward Type"));
        admissionPanel.add(new JLabel(bed.getWardType()));
        
        admissionPanel.add(new JLabel("Assigned Doctor"));
        doctorCombo = new JComboBox<>();
        admissionPanel.add(doctorCombo);
        
        admissionPanel.add(new JLabel("Expected Discharge"));
        expectedDischargeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expectedDischargeSpinner, "yyyy-MM-dd");
        expectedDischargeSpinner.setEditor(dateEditor);
        admissionPanel.add(expectedDischargeSpinner);
        
        mainPanel.add(admissionPanel);
        
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(new JLabel("Admission Notes"));
        notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        mainPanel.add(new JScrollPane(notesArea));

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(new Color(249, 250, 251));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        JButton admitBtn = new JButton("Admit Patient");
        admitBtn.setBackground(new Color(37, 99, 235));
        admitBtn.setForeground(Color.WHITE);
        admitBtn.addActionListener(e -> handleAdmit());
        footer.add(cancelBtn);
        footer.add(admitBtn);
        add(footer, BorderLayout.SOUTH);

        loadDoctors();
    }

    private void loadDoctors() {
        SwingWorkerUtil.execute(
            () -> staffDAO.getDoctors(),
            new SwingWorkerUtil.Callback<List<Staff>>() {
                @Override
                public void onComplete(List<Staff> doctors) {
                    for (Staff s : doctors) doctorCombo.addItem(s);
                }
                @Override public void onError(Exception e) {}
            }
        );
    }

    private void handleAdmit() {
        if (ValidationUtil.isEmpty(nameField.getText()) || ValidationUtil.isEmpty(contactField.getText())) {
            ToastNotification.showError(parent, "Name and Contact are required");
            return;
        }

        Patient p = new Patient();
        p.setName(nameField.getText());
        try { p.setAge(Integer.parseInt(ageField.getText())); } catch (Exception e) { p.setAge(0); }
        p.setContact(contactField.getText());
        p.setBloodGroup((String) bloodGroupCombo.getSelectedItem());
        p.setAddress(addressField.getText());
        p.setEmergencyContact(emContactField.getText());

        Staff doctor = (Staff) doctorCombo.getSelectedItem();
        Date expected = DateUtil.toSqlDate((java.util.Date) expectedDischargeSpinner.getValue());

        SwingWorkerUtil.execute(
            () -> allocationService.admitPatient(p, bed.getWardType(), 
                doctor != null ? doctor.getStaffId() : null, expected, notesArea.getText()),
            new SwingWorkerUtil.Callback<AdmissionDAO.AllocationResult>() {
                @Override
                public void onComplete(AdmissionDAO.AllocationResult result) {
                    if (result.isSuccess()) {
                        ToastNotification.showSuccess(parent, result.getMessage());
                        dashboard.refreshData();
                        dispose();
                    } else {
                        ToastNotification.showError(parent, result.getMessage());
                    }
                }
                @Override
                public void onError(Exception e) {
                    ToastNotification.showError(parent, "Database error: " + e.getMessage());
                }
            }
        );
    }

    private JPanel createHeader(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        p.add(l, BorderLayout.WEST);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        p.setMaximumSize(new Dimension(1000, 30));
        return p;
    }

    private JTextField addField(JPanel p, String label) {
        p.add(new JLabel(label));
        JTextField tf = new JTextField();
        p.add(tf);
        return tf;
    }
}
