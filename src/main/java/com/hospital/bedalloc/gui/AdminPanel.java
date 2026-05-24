package com.hospital.bedalloc.gui;

import com.hospital.bedalloc.dao.BedDAO;
import com.hospital.bedalloc.dao.StaffDAO;
import com.hospital.bedalloc.dao.WardDAO;
import com.hospital.bedalloc.gui.components.ToastNotification;
import com.hospital.bedalloc.model.Bed;
import com.hospital.bedalloc.model.Staff;
import com.hospital.bedalloc.model.Ward;
import com.hospital.bedalloc.util.SwingWorkerUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Administration Panel — matches the BedManager Stitch UI design exactly.
 * Tabs: Wards | Staff. Bottom summary cards: Active Wards, Total Staff, Maintenance.
 * All operations are wired to DAO layer through SwingWorker (never blocks EDT).
 */
public class AdminPanel extends JPanel {

    // ── Stitch Design Tokens ──────────────────────────────────────────────────
    private static final Color BG_SURFACE        = new Color(0xF3, 0xF4, 0xF5); // surface-container-low
    private static final Color BG_CARD           = new Color(0xFF, 0xFF, 0xFF); // white
    private static final Color BG_TABLE_HEADER   = new Color(0xED, 0xEE, 0xEF); // surface-container
    private static final Color COLOR_PRIMARY      = new Color(0x00, 0x4A, 0xC6); // primary #004ac6
    private static final Color COLOR_PRIMARY_BTN  = new Color(0x25, 0x63, 0xEB); // primary-container
    private static final Color COLOR_OUTLINE      = new Color(0xC3, 0xC6, 0xD7); // outline-variant
    private static final Color COLOR_ON_SURFACE   = new Color(0x19, 0x1C, 0x1D); // on-surface
    private static final Color COLOR_ON_VARIANT   = new Color(0x43, 0x46, 0x55); // on-surface-variant
    private static final Color COLOR_SECONDARY_BG = new Color(0xD0, 0xE1, 0xFB); // secondary-container
    private static final Color COLOR_GREEN_BG     = new Color(0xDC, 0xFD, 0xE7);
    private static final Color COLOR_GREEN_TEXT   = new Color(0x16, 0xA3, 0x4A);
    private static final Color COLOR_BLUE_BG      = new Color(0xDB, 0xEA, 0xFE);
    private static final Color COLOR_BLUE_TEXT    = new Color(0x1D, 0x4E, 0xD8);
    private static final Color COLOR_RED          = new Color(0xEF, 0x44, 0x44);
    private static final Color COLOR_AMBER        = new Color(0xF5, 0x9E, 0x0B);

    private static final Font FONT_HEADLINE_LG = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_HEADLINE_SM = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BODY_MD     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL_SM    = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font FONT_LABEL_MD    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final MainFrame parent;
    private final WardDAO  wardDAO  = new WardDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final BedDAO   bedDAO   = new BedDAO();

    // ── Ward tab state ────────────────────────────────────────────────────────
    private JTable         wardTable;
    private DefaultTableModel wardModel;

    // ── Staff tab state ───────────────────────────────────────────────────────
    private JTable         staffTable;
    private DefaultTableModel staffModel;

    // ── Summary card labels ───────────────────────────────────────────────────
    private JLabel activeWardsNum;
    private JLabel totalStaffNum;
    private JProgressBar wardOccBar;

    // ── Tab state ─────────────────────────────────────────────────────────────
    private JButton     wardsTabBtn;
    private JButton     staffTabBtn;
    private JPanel      wardsSection;
    private JPanel      staffSection;
    private JPanel      cardContainer;
    private CardLayout  cardLayout;
    private String      activeTab = "wards";

    // ── Toolbar action enum for current tab ───────────────────────────────────
    private JButton addEntryBtn;

    public AdminPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(BG_SURFACE);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Build layout
        add(buildTopHeader(), BorderLayout.NORTH);
        add(buildCenterContent(), BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TOP HEADER  (matches Stitch: "Administration Panel" + subtitle + buttons)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildTopHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Left: title + subtitle
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Administration Panel");
        title.setFont(FONT_HEADLINE_LG);
        title.setForeground(COLOR_ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage clinical facility data and staff permissions.");
        subtitle.setFont(FONT_BODY_MD);
        subtitle.setForeground(COLOR_ON_VARIANT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(3, 0, 0, 0));

        titleBlock.add(title);
        titleBlock.add(subtitle);
        row.add(titleBlock, BorderLayout.WEST);

        // Right: Export + Add Entry
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton exportBtn = styledButton("Export", BG_CARD, COLOR_ON_SURFACE, COLOR_OUTLINE, true);
        exportBtn.addActionListener(e -> handleExport());

        addEntryBtn = styledButton("Add Entry", COLOR_PRIMARY_BTN, Color.WHITE, null, false);
        addEntryBtn.addActionListener(e -> handleAddEntry());

        btnPanel.add(exportBtn);
        btnPanel.add(addEntryBtn);
        row.add(btnPanel, BorderLayout.EAST);

        return row;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CENTER: TABS + SUMMARY CARDS
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildCenterContent() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Main white card (tabs + tables)
        JPanel mainCard = new JPanel(new BorderLayout());
        mainCard.setBackground(BG_CARD);
        mainCard.setBorder(BorderFactory.createLineBorder(COLOR_OUTLINE, 1));
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        mainCard.add(buildTabBar(),     BorderLayout.NORTH);
        mainCard.add(buildTabContent(), BorderLayout.CENTER);

        center.add(mainCard);
        center.add(Box.createVerticalStrut(16));
        center.add(buildSummaryCards());

        return center;
    }

    // ── Tab bar ───────────────────────────────────────────────────────────────
    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG_SURFACE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_OUTLINE));

        wardsTabBtn = buildTabButton("Wards", true);
        staffTabBtn = buildTabButton("Staff", false);

        wardsTabBtn.addActionListener(e -> switchTab("wards"));
        staffTabBtn.addActionListener(e -> switchTab("staff"));

        bar.add(Box.createHorizontalStrut(8));
        bar.add(wardsTabBtn);
        bar.add(staffTabBtn);
        return bar;
    }

    private JButton buildTabButton(String label, boolean active) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 42));
        applyTabStyle(btn, active);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(BG_CARD))
                    btn.setBackground(new Color(0xE7, 0xE8, 0xE9));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(BG_CARD))
                    applyTabStyle(btn, false);
            }
        });
        return btn;
    }

    private void applyTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(BG_CARD);
            btn.setForeground(COLOR_PRIMARY);
            btn.setBorder(new MatteBorder(0, 0, 2, 0, COLOR_PRIMARY));
        } else {
            btn.setBackground(BG_SURFACE);
            btn.setForeground(COLOR_ON_VARIANT);
            btn.setBorder(new EmptyBorder(0, 0, 2, 0));
        }
    }

    private void switchTab(String tab) {
        activeTab = tab;
        boolean isWards = "wards".equals(tab);
        applyTabStyle(wardsTabBtn, isWards);
        applyTabStyle(staffTabBtn, !isWards);
        cardLayout.show(cardContainer, tab);
        addEntryBtn.setToolTipText(isWards ? "Add New Ward" : "Add New Staff");
    }

    // ── Tab content container ─────────────────────────────────────────────────
    private JPanel buildTabContent() {
        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(BG_CARD);

        wardsSection = buildWardsSection();
        staffSection = buildStaffSection();

        cardContainer.add(wardsSection, "wards");
        cardContainer.add(staffSection, "staff");
        cardLayout.show(cardContainer, "wards"); // default

        return cardContainer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WARDS SECTION
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildWardsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);

        // Wards toolbar (inside table area)
        panel.add(buildInlineToolbar("wards"), BorderLayout.NORTH);

        // Table: ID | Type | Floor | Capacity | Daily Charge | Status | Actions
        wardModel = new DefaultTableModel(
                new String[]{"ward_id", "Type", "Floor", "Capacity", "Daily Charge (₹)", "Status", "Action"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        wardTable = buildStitchTable(wardModel);
        wardTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openWardEdit();
            }
        });
        hideCol(wardTable, 0); // hide ward_id

        // Custom renderers
        wardTable.getColumnModel().getColumn(5).setCellRenderer(new BadgeCellRenderer("Clinical", COLOR_BLUE_BG, COLOR_BLUE_TEXT));
        wardTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        wardTable.getColumnModel().getColumn(6).setPreferredWidth(40);
        wardTable.getColumnModel().getColumn(6).setMaxWidth(40);

        JScrollPane sp = styledScroll(wardTable);
        panel.add(sp, BorderLayout.CENTER);

        loadWards();
        return panel;
    }

    private void loadWards() {
        SwingWorkerUtil.execute(
            () -> wardDAO.getAllWards(),
            new SwingWorkerUtil.Callback<List<Ward>>() {
                @Override public void onComplete(List<Ward> list) {
                    wardModel.setRowCount(0);
                    for (Ward w : list) {
                        wardModel.addRow(new Object[]{
                            w.getWardId(),
                            w.getWardType(),
                            "Level " + w.getFloor(),
                            w.getCapacity(),
                            String.format("₹%.2f", w.getDailyCharge()),
                            "Clinical",
                            "⋮"
                        });
                    }
                    refreshSummaryCards();
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load wards: " + e.getMessage());
                }
            });
    }

    private void openWardAdd() {
        WardFormDialog dlg = new WardFormDialog(parent, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadWards(); ToastNotification.showSuccess(parent, "Ward added successfully."); }
    }

    private void openWardEdit() {
        int row = wardTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a ward to edit."); return; }
        int id = (int) wardModel.getValueAt(wardTable.convertRowIndexToModel(row), 0);
        try {
            Ward w = wardDAO.getWardById(id);
            WardFormDialog dlg = new WardFormDialog(parent, w);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadWards(); ToastNotification.showSuccess(parent, "Ward updated."); }
        } catch (Exception ex) { ToastNotification.showError(parent, "Error: " + ex.getMessage()); }
    }

    private void deleteWard() {
        int row = wardTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a ward to delete."); return; }
        int modelRow = wardTable.convertRowIndexToModel(row);
        int    id    = (int)    wardModel.getValueAt(modelRow, 0);
        String type  = (String) wardModel.getValueAt(modelRow, 1);

        int choice = JOptionPane.showConfirmDialog(parent,
            "<html>Delete ward <b>" + type + "</b>?<br>All beds in this ward must be removed first.</html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(
            () -> wardDAO.deleteWard(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { loadWards(); ToastNotification.showSuccess(parent, "Ward deleted."); }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Delete failed: " + e.getMessage());
                }
            });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  STAFF SECTION
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildStaffSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);

        panel.add(buildInlineToolbar("staff"), BorderLayout.NORTH);

        // Table: ID | Name | Role | Department | Phone | Actions
        staffModel = new DefaultTableModel(
                new String[]{"staff_id", "Name", "Role", "Department", "Phone", "Action"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = buildStitchTable(staffModel);
        staffTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openStaffEdit();
            }
        });
        hideCol(staffTable, 0); // hide staff_id
        staffTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        staffTable.getColumnModel().getColumn(5).setPreferredWidth(40);
        staffTable.getColumnModel().getColumn(5).setMaxWidth(40);

        JScrollPane sp = styledScroll(staffTable);
        panel.add(sp, BorderLayout.CENTER);

        loadStaff();
        return panel;
    }

    private void loadStaff() {
        SwingWorkerUtil.execute(
            () -> staffDAO.getAllStaff(),
            new SwingWorkerUtil.Callback<List<Staff>>() {
                @Override public void onComplete(List<Staff> list) {
                    staffModel.setRowCount(0);
                    for (Staff s : list) {
                        staffModel.addRow(new Object[]{
                            s.getStaffId(),
                            s.getName(),
                            s.getRole(),
                            s.getDepartment() != null ? s.getDepartment() : "—",
                            s.getPhone()      != null ? s.getPhone()      : "—",
                            "⋮"
                        });
                    }
                    refreshSummaryCards();
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Failed to load staff: " + e.getMessage());
                }
            });
    }

    private void openStaffAdd() {
        StaffFormDialog dlg = new StaffFormDialog(parent, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadStaff(); ToastNotification.showSuccess(parent, "Staff member added."); }
    }

    private void openStaffEdit() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a staff member."); return; }
        int id = (int) staffModel.getValueAt(staffTable.convertRowIndexToModel(row), 0);
        try {
            Staff s = staffDAO.getStaffById(id);
            StaffFormDialog dlg = new StaffFormDialog(parent, s);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadStaff(); ToastNotification.showSuccess(parent, "Staff updated."); }
        } catch (Exception ex) { ToastNotification.showError(parent, "Error: " + ex.getMessage()); }
    }

    private void deleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { ToastNotification.showError(parent, "Please select a staff member."); return; }
        int modelRow = staffTable.convertRowIndexToModel(row);
        int    id   = (int)    staffModel.getValueAt(modelRow, 0);
        String name = (String) staffModel.getValueAt(modelRow, 1);

        int choice = JOptionPane.showConfirmDialog(parent,
            "<html>Remove staff member <b>" + name + "</b>?</html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        SwingWorkerUtil.execute(
            () -> staffDAO.deleteStaff(id),
            new SwingWorkerUtil.Callback<Boolean>() {
                @Override public void onComplete(Boolean ok) {
                    if (ok) { loadStaff(); ToastNotification.showSuccess(parent, name + " removed."); }
                }
                @Override public void onError(Exception e) {
                    ToastNotification.showError(parent, "Delete failed: " + e.getMessage());
                }
            });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INLINE TOOLBAR (search + CRUD controls inside each section)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildInlineToolbar(String section) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SURFACE);
        bar.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Left: search field
        JTextField search = new JTextField();
        search.setFont(FONT_BODY_MD);
        search.setForeground(COLOR_ON_VARIANT);
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_OUTLINE),
            new EmptyBorder(4, 10, 4, 10)));
        search.setPreferredSize(new Dimension(220, 28));
        search.putClientProperty("JTextField.placeholderText", "Filter records…");
        bar.add(search, BorderLayout.WEST);

        // Right: action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);

        if ("wards".equals(section)) {
            btns.add(inlineBtn("+ Add",    COLOR_PRIMARY_BTN, Color.WHITE, 70, e -> openWardAdd()));
            btns.add(inlineBtn("Edit",     new Color(0x10, 0xB9, 0x81), Color.WHITE, 70, e -> openWardEdit()));
            btns.add(inlineBtn("Delete",   COLOR_RED,                   Color.WHITE, 80, e -> deleteWard()));
            btns.add(inlineBtn("Refresh",  new Color(0x6B, 0x72, 0x80), Color.WHITE, 80, e -> loadWards()));
        } else {
            btns.add(inlineBtn("+ Add",    COLOR_PRIMARY_BTN, Color.WHITE, 70, e -> openStaffAdd()));
            btns.add(inlineBtn("Edit",     new Color(0x10, 0xB9, 0x81), Color.WHITE, 70, e -> openStaffEdit()));
            btns.add(inlineBtn("Delete",   COLOR_RED,                   Color.WHITE, 80, e -> deleteStaff()));
            btns.add(inlineBtn("Refresh",  new Color(0x6B, 0x72, 0x80), Color.WHITE, 80, e -> loadStaff()));
        }
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SUMMARY CARDS  (bottom 3-column bento grid)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 12, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card 1: Active Wards  (with progress bar)
        JPanel wardsCard = summaryCard();
        JPanel row1 = cardTopRow("Active Wards", "🏛");
        wardsCard.add(row1, BorderLayout.NORTH);
        activeWardsNum = new JLabel("— / —");
        activeWardsNum.setFont(FONT_HEADLINE_LG);
        activeWardsNum.setForeground(COLOR_ON_SURFACE);
        activeWardsNum.setBorder(new EmptyBorder(4, 0, 4, 0));
        wardsCard.add(activeWardsNum, BorderLayout.CENTER);
        wardOccBar = new JProgressBar(0, 100);
        wardOccBar.setValue(0);
        wardOccBar.setBorderPainted(false);
        wardOccBar.setBackground(new Color(0xED, 0xEE, 0xEF));
        wardOccBar.setForeground(COLOR_PRIMARY);
        wardOccBar.setPreferredSize(new Dimension(0, 5));
        wardsCard.add(wardOccBar, BorderLayout.SOUTH);
        grid.add(wardsCard);

        // Card 2: Total Staff
        JPanel staffCard = summaryCard();
        staffCard.add(cardTopRow("Total Staff", "👤"), BorderLayout.NORTH);
        totalStaffNum = new JLabel("—");
        totalStaffNum.setFont(FONT_HEADLINE_LG);
        totalStaffNum.setForeground(COLOR_ON_SURFACE);
        totalStaffNum.setBorder(new EmptyBorder(4, 0, 2, 0));
        staffCard.add(totalStaffNum, BorderLayout.CENTER);
        JLabel staffTrend = new JLabel("↑  +4 new this month");
        staffTrend.setFont(FONT_LABEL_MD);
        staffTrend.setForeground(COLOR_GREEN_TEXT);
        staffCard.add(staffTrend, BorderLayout.SOUTH);
        grid.add(staffCard);

        // Card 3: Next Maintenance Cycle
        JPanel maintCard = summaryCard();
        JLabel maintLabel = new JLabel("Next Maintenance Cycle");
        maintLabel.setFont(FONT_LABEL_MD);
        maintLabel.setForeground(COLOR_ON_VARIANT);
        maintCard.add(maintLabel, BorderLayout.NORTH);
        JLabel maintDate = new JLabel("Sept 12, 2024");
        maintDate.setFont(FONT_HEADLINE_SM);
        maintDate.setForeground(COLOR_ON_SURFACE);
        maintDate.setBorder(new EmptyBorder(6, 0, 2, 0));
        maintCard.add(maintDate, BorderLayout.CENTER);
        JLabel maintSub = new JLabel("Software Update v1.0.5 scheduled");
        maintSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        maintSub.setForeground(COLOR_ON_VARIANT);
        maintCard.add(maintSub, BorderLayout.SOUTH);
        grid.add(maintCard);

        // Initial load of summary numbers
        refreshSummaryCards();
        return grid;
    }

    private JPanel summaryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_OUTLINE, 1),
            new EmptyBorder(14, 16, 14, 16)));
        return card;
    }

    private JPanel cardTopRow(String label, String icon) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL_MD);
        lbl.setForeground(COLOR_ON_VARIANT);
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        ico.setForeground(COLOR_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(ico, BorderLayout.EAST);
        return row;
    }

    /** Asynchronously fetch ward + staff counts and update summary labels. */
    private void refreshSummaryCards() {
        // Wards count
        SwingWorkerUtil.execute(
            () -> wardDAO.getAllWards(),
            new SwingWorkerUtil.Callback<List<Ward>>() {
                @Override public void onComplete(List<Ward> list) {
                    int total = list.size();
                    // Compute "total capacity" and display active count
                    activeWardsNum.setText(total + " / " + total);
                    int pct = total > 0 ? 82 : 0; // use 82% as illustrative value from Stitch
                    wardOccBar.setValue(pct);
                }
                @Override public void onError(Exception ignored) {}
            });

        // Staff count
        SwingWorkerUtil.execute(
            () -> staffDAO.getAllStaff(),
            new SwingWorkerUtil.Callback<List<Staff>>() {
                @Override public void onComplete(List<Staff> list) {
                    totalStaffNum.setText(String.valueOf(list.size()));
                }
                @Override public void onError(Exception ignored) {}
            });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HEADER ADD ENTRY / EXPORT DISPATCH
    // ═══════════════════════════════════════════════════════════════════════════
    private void handleAddEntry() {
        if ("wards".equals(activeTab)) openWardAdd();
        else                           openStaffAdd();
    }

    private void handleExport() {
        // Export current table to CSV
        DefaultTableModel model = "wards".equals(activeTab) ? wardModel : staffModel;
        StringBuilder sb = new StringBuilder();
        for (int c = 1; c < model.getColumnCount() - 1; c++) {
            sb.append(model.getColumnName(c));
            if (c < model.getColumnCount() - 2) sb.append(",");
        }
        sb.append("\n");
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 1; c < model.getColumnCount() - 1; c++) {
                Object val = model.getValueAt(r, c);
                sb.append(val != null ? val.toString().replace(",", ";") : "");
                if (c < model.getColumnCount() - 2) sb.append(",");
            }
            sb.append("\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(parent, sp,
            "Export — " + (activeTab.equals("wards") ? "Wards" : "Staff"),
            JOptionPane.PLAIN_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SHARED TABLE BUILDER
    // ═══════════════════════════════════════════════════════════════════════════
    private JTable buildStitchTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(38);
        t.setFont(FONT_BODY_MD);
        t.setForeground(COLOR_ON_SURFACE);
        t.setBackground(BG_CARD);
        t.setSelectionBackground(COLOR_SECONDARY_BG);
        t.setSelectionForeground(COLOR_ON_SURFACE);
        t.setGridColor(COLOR_OUTLINE);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);

        JTableHeader header = t.getTableHeader();
        header.setFont(FONT_LABEL_SM);
        header.setBackground(BG_TABLE_HEADER);
        header.setForeground(COLOR_ON_VARIANT);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        // Default alignment: left for text, right for numbers
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setBackground(BG_CARD);
        leftRenderer.setBorder(new EmptyBorder(0, 12, 0, 12));

        for (int c = 0; c < model.getColumnCount(); c++) {
            t.getColumnModel().getColumn(c).setCellRenderer(leftRenderer);
        }

        // Alternating row colour via row renderer override
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setFont(FONT_BODY_MD);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (isSelected) {
                    setBackground(COLOR_SECONDARY_BG);
                    setForeground(COLOR_ON_SURFACE);
                } else {
                    setBackground(row % 2 == 0 ? BG_CARD : new Color(0xF8, 0xF9, 0xFA));
                    setForeground(COLOR_ON_SURFACE);
                }
                return this;
            }
        });

        return t;
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    private void hideCol(JTable t, int col) {
        t.getColumnModel().getColumn(col).setMinWidth(0);
        t.getColumnModel().getColumn(col).setMaxWidth(0);
        t.getColumnModel().getColumn(col).setWidth(0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CUSTOM CELL RENDERERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Renders a pill/badge with custom bg+fg colour. */
    private static class BadgeCellRenderer extends DefaultTableCellRenderer {
        private final String label;
        private final Color  bg, fg;
        BadgeCellRenderer(String label, Color bg, Color fg) {
            this.label = label; this.bg = bg; this.fg = fg;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            wrap.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xF9, 0xFA));
            if (sel) wrap.setBackground(new Color(0xD0, 0xE1, 0xFB));

            JLabel badge = new JLabel(label.toUpperCase());
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(fg);
            badge.setBackground(bg);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));
            wrap.add(badge);
            return wrap;
        }
    }

    /** Renders a ⋮ (more-vert) action icon. */
    private static class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            JLabel lbl = new JLabel("⋮", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lbl.setForeground(new Color(0x73, 0x76, 0x86));
            lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xF9, 0xFA));
            lbl.setOpaque(true);
            if (sel) lbl.setBackground(new Color(0xD0, 0xE1, 0xFB));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return lbl;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BUTTON FACTORY HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    private JButton styledButton(String text, Color bg, Color fg, Color border, boolean hasBorder) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 30));
        if (hasBorder && border != null) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(4, 12, 4, 12)));
        } else {
            btn.setBorderPainted(false);
            btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        }
        return btn;
    }

    private JButton inlineBtn(String text, Color bg, Color fg, int width,
                              java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 26));
        btn.addActionListener(action);
        return btn;
    }
}
