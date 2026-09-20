import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Shared visual system for the desktop and browser builds. */
public final class AppTheme {
    public static final Color BACKGROUND = new Color(15, 23, 42);
    public static final Color SURFACE = new Color(30, 41, 59);
    public static final Color SURFACE_LIGHT = new Color(51, 65, 85);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color TEXT = new Color(241, 245, 249);
    public static final Color MUTED = new Color(148, 163, 184);
    public static final Color BORDER = new Color(71, 85, 105);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font SUBTITLE = new Font("SansSerif", Font.PLAIN, 13);

    private AppTheme() {}

    public static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY_BOLD);
        UIManager.put("Button.background", SURFACE_LIGHT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextArea.font", BODY);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("ComboBox.background", SURFACE_LIGHT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.font", BODY);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", PRIMARY);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", SURFACE_LIGHT);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("TableHeader.font", BODY_BOLD);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY);
        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("Viewport.background", SURFACE);
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(primary ? PRIMARY : SURFACE_LIGHT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primary ? PRIMARY : BORDER),
            padding(10, 16, 10, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setReorderingAllowed(false);
    }

    public static JPanel pageHeader(String eyebrow, String title, String description) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel eyebrowLabel = new JLabel(eyebrow.toUpperCase());
        eyebrowLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrowLabel.setForeground(new Color(96, 165, 250));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE);
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(SUBTITLE);
        descriptionLabel.setForeground(MUTED);
        header.add(eyebrowLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(descriptionLabel);
        return header;
    }
}
