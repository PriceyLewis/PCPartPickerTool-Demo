import javax.swing.*;
import java.awt.*;

public class GUI {
    private final Font defaultFont = AppTheme.BODY;

    // Formats frames for a consistent, non-flickery setup.
    public JFrame Addframe(int width, int height, String title) {
        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(width, height));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(AppTheme.BACKGROUND);
        return frame;
    }

    public JPanel Addpanel(JFrame frame, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(AppTheme.padding(20, 20, 20, 20));
        panel.setBackground(AppTheme.BACKGROUND);
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
        return panel;
    }

    public JPanel Addpanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setBorder(AppTheme.padding(20, 20, 20, 20));
        panel.setBackground(AppTheme.BACKGROUND);
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
        return panel;
    }

    public JLabel Addlabel(String text, JPanel panel) {
        JLabel label = new JLabel(text);
        label.setFont(defaultFont);
        panel.add(label);
        return label;
    }

    public JButton Addbutton(String text, JPanel panel) {
        JButton button = AppTheme.button(text, false);
        panel.add(button);
        return button;
    }

    public JTextField AddtextField(JPanel panel) {
        JTextField textField = new JTextField();
        textField.setFont(defaultFont);
        textField.setPreferredSize(new Dimension(200, 30));
        panel.add(textField);
        return textField;
    }

    public JTextArea AddtextArea(JPanel panel) {
        JTextArea textArea = new JTextArea(5, 20);
        textArea.setFont(defaultFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(textArea));
        return textArea;
    }

    public void Close(JFrame frame) {
        frame.dispose();
    }

    public String AddOptionPane(String prompt) {
        return JOptionPane.showInputDialog(null, prompt);
    }
}
