package COI_Implement;

import javax.swing.*;

import COI_Implement.pdp_pep.PdpAndPep;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;

public class SwingApp extends JFrame {
    private JTextField xacmlFileField, txtFileField, javaFileField,javaFileFieldABAC, ABACFileField //checkxacml//
            , javaFile2;
    private JTextArea inputArea,inputArea2,inputArea3,inputAreaABAC
            , resultArea, resultXacmlToTxtArea, resultTxtToXacmlArea,resultTxtToXacmlArea2,resultCheckXacmlArea,resultArea2,resultAreaABAC;
    private String selectedFilePath, selectedXacmlFilePath, selectedTxtFilePath,selectedFilePathABAC,selectedFilePath2;

    public SwingApp() {
        setTitle("VerCol");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Xacml2Txt", createXacmlToTxtPanel());
        tabbedPane.addTab("Txt2Xacml", createTxtToXacmlPanel());
        tabbedPane.addTab("Consistency Verification", createCheckXacmlPanel());
        tabbedPane.addTab("Compliance Verification", createRbacPanel());
        tabbedPane.addTab("ABAC", createABACPanel());

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // giao diện ABAC
    // Thêm biến để lưu đường dẫn

    private JPanel createABACPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        Font labelFont = new Font("Serif", Font.BOLD, 14);

        // Label "XACML Specifications:"
        JLabel inputLabel = new JLabel("<html>XACML<br>Specifications:</html>");
        inputLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(inputLabel, gbc);

        // TextArea "XACML Specifications"
        inputAreaABAC = new JTextArea(8, 50);
        inputAreaABAC.setLineWrap(true);
        inputAreaABAC.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputAreaABAC);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        // Panel chứa Select XACML File + Browse
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel selectLabel = new JLabel("Select XACML file:");
        selectLabel.setFont(labelFont);
        JButton browseButton = new JButton("Browse");

        actionPanel.add(selectLabel);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(browseButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(actionPanel, gbc);

        // Label "System Sourcecode:"
        JLabel resultLabel2 = new JLabel("<html>System<br>Implememtation:</html>");
        resultLabel2.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(resultLabel2, gbc);

        // TextField để hiển thị đường dẫn System Sourcecode
        javaFileFieldABAC = new JTextField(40);
        javaFileFieldABAC.setEditable(false);
        javaFileFieldABAC.setBackground(Color.WHITE);
        javaFileFieldABAC.setDisabledTextColor(Color.BLACK);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(javaFileFieldABAC, gbc);

        // Panel chứa Select Java File + Browse + Verify
        JPanel javaFilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel selectJavaLabel = new JLabel("Select source code:");
        selectJavaLabel.setFont(labelFont);
        JButton browseJavaButton = new JButton("Browse");
        JButton checkButton = new JButton("Verify");

        javaFilePanel.add(selectJavaLabel);
        javaFilePanel.add(Box.createHorizontalStrut(10));
        javaFilePanel.add(browseJavaButton);
        javaFilePanel.add(Box.createHorizontalStrut(10));
        javaFilePanel.add(checkButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(javaFilePanel, gbc);

        // Label "Notification of Results:"
        JLabel resultLabel = new JLabel("<html>Verification<br>Results:</html>");
        resultLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(resultLabel, gbc);

        // TextArea "Notification of Results"
        resultAreaABAC = new JTextArea(8, 50);
        resultAreaABAC.setEditable(false);
        resultAreaABAC.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultAreaABAC);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(resultScrollPane, gbc);

        // Bắt sự kiện các nút
        browseButton.addActionListener(e -> selectFileForInputABAC());
        browseJavaButton.addActionListener(e -> selectJavaFileABAC());
        checkButton.addActionListener(e -> checkABAC());

        return panel;
    }




    // load file java vào giao diện ABAC
    private void selectJavaFileABAC() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            javaFileFieldABAC.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    // load file xacml vào giao diện (ABAC)
    private void selectFileForInputABAC() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePathABAC = selectedFile.getAbsolutePath();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                inputAreaABAC.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
            }
        }
    }

    private void checkABAC() {
        if (selectedFilePathABAC == null || selectedFilePathABAC.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select an XACML file before converting.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PdpAndPep.CheckPdpAndPep(resultAreaABAC,selectedFilePathABAC);
    }





    //giao diện kiểm tra xacml
    private JPanel createCheckXacmlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        Font labelFont = new Font("Serif", Font.BOLD, 14);

        // Label "XACML Specification" bên trái
        JLabel inputLabel = new JLabel("<html>XACML<br>Specifications:</html>");
        inputLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        panel.add(inputLabel, gbc);

        // Ô nhập nội dung XACML
        inputArea3 = new JTextArea(10, 50);
        inputArea3.setLineWrap(true);
        inputArea3.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea3);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        // Dòng chọn file (đặt giữa 2 ô)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel selectFileLabel = new JLabel("Select file Xacml:");
        buttonPanel.add(selectFileLabel);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> selectFileForInput2());
        buttonPanel.add(browseButton);

        JButton checkButton = new JButton("Verify");
        checkButton.addActionListener(e -> checkXacmlFile());
        buttonPanel.add(checkButton);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Label "Verification Results" bên trái
        JLabel resultLabel = new JLabel("<html>Verification<br>Results:</html>");
        resultLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(resultLabel, gbc);


        // Ô hiển thị kết quả
        resultCheckXacmlArea = new JTextArea(10, 50);
        resultCheckXacmlArea.setEditable(false);
        resultCheckXacmlArea.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultCheckXacmlArea);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(resultScrollPane, gbc);

        return panel;
    }

    // Hàm đọc nội dung file XACML và hiển thị lên inputArea3
    private void selectFileForInput2() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedXacmlFilePath = selectedFile.getAbsolutePath(); // Lưu đường dẫn file

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                inputArea3.read(reader, null); // Hiển thị nội dung lên giao diện
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Lỗi khi đọc file: " + e.getMessage());
            }
        }
    }

    private void checkXacmlFile() {
        if (selectedXacmlFilePath == null || selectedXacmlFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Lỗi: Chưa chọn file XACML!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Gọi hàm kiểm tra với đường dẫn file
            CheckXacml.checkFileForConflicts(resultCheckXacmlArea, selectedXacmlFilePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi kiểm tra XACML: " + e.getMessage(), "Check Failed", JOptionPane.ERROR_MESSAGE);
        }
    }



    // giao diện chuyển đổi xacml -> txt
    private JPanel createXacmlToTxtPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        xacmlFileField = new JTextField(30);
        JButton browseButton = new JButton("Browse");
        JButton convertButton = new JButton("Convert");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedXacmlFilePath = fileChooser.getSelectedFile().getAbsolutePath(); // Lưu đường dẫn
                xacmlFileField.setText(selectedXacmlFilePath); // Hiển thị trong text field
            }
        });

        convertButton.addActionListener(e -> convertXacmlToTxt());

        panel.add(new JLabel("Select XACML File:"));
        panel.add(xacmlFileField);
        panel.add(browseButton);
        panel.add(convertButton);

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultXacmlToTxtArea = new JTextArea(5, 50);
        resultXacmlToTxtArea.setEditable(false);
        resultXacmlToTxtArea.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultXacmlToTxtArea);
        resultPanel.add(resultScrollPane);
        panel.add(resultPanel);

        return panel;
    }

    //giao diện chuển đổi txt->xacml
    private JPanel createTxtToXacmlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        Font labelFont = new Font("Serif", Font.BOLD, 14);

        // Label "Access Control Rules:" xuống thành 2 dòng
        JLabel inputLabel = new JLabel("<html>Access Control<br>Rules:</html>");
        inputLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(inputLabel, gbc);

        // TextArea "Access Control Rules"
        inputArea2 = new JTextArea(7, 50);
        inputArea2.setLineWrap(true);
        inputArea2.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea2);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        // Panel chứa Select TXT File + Browse + Convert (căn giữa)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Căn giữa
        JLabel selectLabel = new JLabel("Select rule file:");
        selectLabel.setFont(labelFont);
        JButton browseButton = new JButton("Browse");
        JButton convertButton = new JButton("Convert");

        actionPanel.add(selectLabel);
        actionPanel.add(Box.createHorizontalStrut(10)); // Thêm khoảng cách giữa label và nút
        actionPanel.add(browseButton);
        actionPanel.add(convertButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER; // Đưa toàn bộ hàng này vào giữa
        panel.add(actionPanel, gbc);

        // Label "XACML Specification:" xuống thành 2 dòng
        JLabel resultLabel2 = new JLabel("<html>XACML<br>Specifications:</html>");
        resultLabel2.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(resultLabel2, gbc);

        // TextArea "XACML Specification"
        resultTxtToXacmlArea2 = new JTextArea(7, 50);
        resultTxtToXacmlArea2.setEditable(false);
        resultTxtToXacmlArea2.setBackground(Color.WHITE);
        JScrollPane resultScrollPane2 = new JScrollPane(resultTxtToXacmlArea2);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(resultScrollPane2, gbc);

        // Label "Notification of Results:" xuống thành 2 dòng
        JLabel resultLabel = new JLabel("<html>Notification of<br>Results:</html>");
        resultLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(resultLabel, gbc);

        // TextArea "Notification of Results"
        resultTxtToXacmlArea = new JTextArea(3, 50);
        resultTxtToXacmlArea.setEditable(false);
        resultTxtToXacmlArea.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultTxtToXacmlArea);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(resultScrollPane, gbc);

        // Bắt sự kiện nút bấm
        browseButton.addActionListener(e -> selectTxtFile());
        convertButton.addActionListener(e -> convertTxtToXacml());

        return panel;
    }



    // Hàm chọn file TXT và hiển thị nội dung lên inputArea trong giao diện RBAC
    private void selectTxtFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedTxtFilePath = fileChooser.getSelectedFile().getAbsolutePath();

            // Kiểm tra file có tồn tại không
            File file = new File(selectedTxtFilePath);
            if (!file.exists() || !file.canRead()) {
                inputArea2.setText("Lỗi: Không thể đọc file!");
                return;
            }

            // Đọc file và hiển thị nội dung lên inputArea
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedTxtFilePath))) {
                inputArea2.setText(""); // Xóa nội dung cũ
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                inputArea2.setText(content.toString()); // Cập nhật nội dung
                inputArea2.setCaretPosition(0); // Cuộn lên đầu
                inputArea2.revalidate();
                inputArea2.repaint(); // Cập nhật giao diện
            } catch (IOException ex) {
                inputArea2.setText("Lỗi khi đọc file: " + ex.getMessage());
            }
        }
    }

    // Giao diện RBAC
    private JPanel createRbacPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Text or Xacml file:"));

        inputArea = new JTextArea(5, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        inputPanel.add(scrollPane);

        JButton browseButton = new JButton("Browse");
        inputPanel.add(browseButton);
        browseButton.addActionListener(e -> selectFileForInput());

        panel.add(inputPanel);

        JPanel javaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        javaPanel.add(new JLabel("Select Java File:"));
        javaFileField = new JTextField( 30);
        javaPanel.add(javaFileField);
        JButton javaBrowseButton = new JButton("Browse");
        javaPanel.add(javaBrowseButton);
        javaBrowseButton.addActionListener(e -> selectJavaFile());
        panel.add(javaPanel);

        JPanel checkPanel = new JPanel();
        JButton checkButton = new JButton("Check");
        checkPanel.add(checkButton);
        checkButton.addActionListener(e -> {
            try {
                checkContent();
            } catch (ParserConfigurationException | SAXException ex) {
                throw new RuntimeException(ex);
            }
        });
        panel.add(checkPanel);

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultArea = new JTextArea(5, 50);
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultPanel.add(resultScrollPane);
        panel.add(resultPanel);

        return panel;
    }

    private void selectFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // load file xacml vào giao diện Compliance Verification(RBAC)
    private void selectFileForInput() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                inputArea.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
            }
        }
    }

    // load file java vào giao diện Compliance Verification (RBAC)
    private void selectJavaFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            javaFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }



    private void convertXacmlToTxt() {
        if (selectedXacmlFilePath == null || selectedXacmlFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select an XACML file before converting.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            XacmlToTxt.checkFileForConflicts(resultXacmlToTxtArea,selectedXacmlFilePath);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
//    private void convertTxtToXacml() {
//        if (selectedTxtFilePath == null || selectedTxtFilePath.isEmpty()) {
//            JOptionPane.showMessageDialog(null, "Please select a TXT file before converting.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        try {
//            TxtToXacml.checkFileForConflicts(resultTxtToXacmlArea,selectedTxtFilePath);
//        } catch (IOException | ParserConfigurationException | SAXException e) {
//            throw new RuntimeException(e);
//        }
//    }
    private void convertTxtToXacml() {
    if (selectedTxtFilePath == null || selectedTxtFilePath.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Please select a TXT file before converting.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select Directory to Save XACML File");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int userSelection = fileChooser.showSaveDialog(null);
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File selectedDirectory = fileChooser.getSelectedFile();
        String outputDirectory = selectedDirectory.getAbsolutePath();

        try {
            TxtToXacml.checkFileForConflicts(resultTxtToXacmlArea,resultTxtToXacmlArea2, selectedTxtFilePath, outputDirectory);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Conversion Failed", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(null, "No directory selected. Conversion cancelled.", "Warning", JOptionPane.WARNING_MESSAGE);
    }
}

    private void checkContent() throws ParserConfigurationException, SAXException {
        if (selectedFilePath != null || selectedFilePath2 !=null ) {
            try {
                ConflictChecker.checkFileForConflicts(resultArea,resultAreaABAC, selectedFilePath);
                ConflictChecker.checkFileForConflicts(resultArea2, resultAreaABAC, selectedFilePath2);
            } catch (IOException e) {
                resultArea.setText("Error: " + e.getMessage());
                resultAreaABAC.setText("Error: " + e.getMessage());
                resultArea2.setText("Error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
        }
    }

    public static void main(String[] args) {
        new SwingApp();
    }
}
