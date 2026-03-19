import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
        setText(value == null ? "Add to Basket" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private final DefaultTableModel model;
    private JTable table;
    private String label;

    public ButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
        super(checkBox);
        this.model = model;
        this.button = new JButton("Add to Basket");
        this.button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.label = value == null ? "Add to Basket" : value.toString();
        button.setText(label);
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String partName = String.valueOf(model.getValueAt(row, 0));
            String storeName = String.valueOf(model.getValueAt(row, 1));
            double price = Main.parsePriceValue(model.getValueAt(row, 2));
            Main.addItemToBasket(partName, storeName, price, table);
        }
        return label;
    }
}
