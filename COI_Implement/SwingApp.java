package COI_Implement;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SwingApp extends JFrame {
    private final JTextField javaFileField;
    private final JTextArea inputArea;
    private final JButton browseButton;
    private final JButton javaBrowseButton;
    private final JButton checkButton;
    private final JTextArea resultArea;
    private String selectedFilePath; // Biến để lưu đường dẫn file đã chọn

    public SwingApp() {
        this.setTitle("TOOL");
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel inputLabel = new JLabel("Enter text or select a XACML file:");
        inputPanel.add(inputLabel);

        inputArea = new JTextArea(5, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        inputPanel.add(scrollPane);

        browseButton = new JButton("Browse");
        inputPanel.add(browseButton);
        browseButton.addActionListener(e -> selectFile());

        this.add(inputPanel);

        JPanel javaPanel = new JPanel();
        javaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel javaFileLabel = new JLabel("Select Java File:");
        javaPanel.add(javaFileLabel);

        javaFileField = new JTextField(30);
        javaPanel.add(javaFileField);

        javaBrowseButton = new JButton("Browse");
        javaPanel.add(javaBrowseButton);
        javaBrowseButton.addActionListener(e -> selectJavaFile());

        this.add(javaPanel);

        JPanel checkPanel = new JPanel();
        checkButton = new JButton("Check");
        checkPanel.add(checkButton);
        checkButton.addActionListener(e -> {
            try {
                checkContent();
            } catch (ParserConfigurationException | SAXException ex) {
                throw new RuntimeException(ex);
            }
        }); // Gọi checkContent

        this.add(checkPanel);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        resultArea = new JTextArea(5, 50);
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultPanel.add(resultScrollPane);

        this.add(resultPanel);

        this.setVisible(true);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath(); // Lưu đường dẫn của file đã chọn
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                inputArea.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
            }
        }
    }

    private void selectJavaFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedJavaFile = fileChooser.getSelectedFile();
            javaFileField.setText(selectedJavaFile.getAbsolutePath());
        }
    }

    private void checkContent() throws ParserConfigurationException, SAXException {
        if (selectedFilePath != null) {
            try {
                ConflictChecker.checkFileForConflicts(resultArea, selectedFilePath); // Truyền đường dẫn file đã chọn
            } catch (IOException e) {
                resultArea.setText("Đã xảy ra lỗi: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
        }
    }

    public static void main(String[] args) {
        new SwingApp();
    }
}
