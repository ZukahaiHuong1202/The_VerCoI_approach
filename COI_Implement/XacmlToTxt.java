package COI_Implement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class XacmlToTxt {
    public static void checkFileForConflicts(JTextArea resultArea, String filePath) throws IOException, ParserConfigurationException, SAXException {

        // Kiểm tra phần mở rộng của file
        if (!filePath.endsWith(".xacml")) {
            resultArea.setText("ERROR: Support \".xacml\" format File only!");
            return; // Dừng việc đọc file nếu không phải là file .xacml
        }

        // Kiểm tra nếu file là XACML (XML hợp lệ)
        if (!isValidXacmlFile(filePath)) {
            resultArea.setText("ERROR: File has invalid \".xacml\" format.");
            return;
        }

        String outputFilePath = "outputXacmlToTxt.txt"; // Đường dẫn file đầu ra

        // Đọc file XACML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(filePath);
        document.getDocumentElement().normalize();

        // Tạo Map để ánh xạ dữ liệu
        Map<String, Map<String, String>> userData = new LinkedHashMap<>();

        // Duyệt qua các Rule
        NodeList ruleNodes = document.getElementsByTagName("Rule");
        for (int i = 0; i < ruleNodes.getLength(); i++) {
            Element rule = (Element) ruleNodes.item(i);
            String userId = rule.getAttribute("RuleId");

            // Ánh xạ các cột trong ObligationExpression
            Map<String, String> columns = new LinkedHashMap<>();
            NodeList obligations = rule.getElementsByTagName("ObligationExpression");
            for (int j = 0; j < obligations.getLength(); j++) {
                Element obligation = (Element) obligations.item(j);
                String columnId = obligation.getAttribute("ObligationId");

                // Lấy giá trị trong AttributeAssignmentExpression
                NodeList assignments = obligation.getElementsByTagName("AttributeAssignmentExpression");
                List<String> values = new ArrayList<>();
                for (int k = 0; k < assignments.getLength(); k++) {
                    Element assignment = (Element) assignments.item(k);
                    String value = assignment.getTextContent().trim();
                    values.add(value);
                }

                // Gộp các giá trị vào 1 cột (ngăn cách bởi dấu phẩy)
                columns.put(columnId, String.join(",", values));
            }

            // Thêm vào Map
            userData.put(userId, columns);
        }

        // Ghi dữ liệu ra file
        writeToFile(outputFilePath, userData);
        resultArea.setText("Convert Success!");
    }

    public static boolean isValidXacmlFile(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.parse(xmlFile); // Kiểm tra xem file có thể phân tích cú pháp không
            return true; // Nếu không có lỗi, file là hợp lệ
        } catch (Exception e) {
            return false; // Nếu có lỗi, file không hợp lệ
        }
    }

    private static void writeToFile(String filePath, Map<String, Map<String, String>> userData) throws IOException {
        FileWriter writer = new FileWriter(filePath);

        // Tạo header từ các cột
        Set<String> columns = new LinkedHashSet<>();
        userData.values().forEach(map -> columns.addAll(map.keySet()));

        writer.write("username," + String.join(",", columns) + "\n");

        // Ghi từng dòng dữ liệu
        for (Map.Entry<String, Map<String, String>> entry : userData.entrySet()) {
            String username = entry.getKey();
            Map<String, String> userColumns = entry.getValue();

            List<String> row = new ArrayList<>();
            row.add(username);
            for (String column : columns) {
                row.add("{" + userColumns.getOrDefault(column, "") + "}");
            }
            writer.write(String.join(",", row) + "\n");
        }

        writer.close();
    }
}
