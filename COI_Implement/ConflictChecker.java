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

public class ConflictChecker {

    public static Map<Integer,OtherUser> DataMap = new LinkedHashMap<>();

    public static RoleBasedAccessControl roleBasedAccessControl = new RoleBasedAccessControl();

    // Thay đổi để nhận đường dẫn file từ tham số
    public static void checkFileForConflicts(JTextArea resultArea, JTextArea resultAreaABAC, String filePath) throws IOException, ParserConfigurationException, SAXException {

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

        String outputFilePath = "output.txt"; // Đường dẫn file đầu ra

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

        try (BufferedReader br = new BufferedReader(new FileReader(outputFilePath))) {
            String line;

            br.readLine();
            int num = 0;
            while ((line = br.readLine()) != null) {

                // Tách chuỗi thành các phần tử
                String[] parts = line.split(",(?=\\s*\\{)");
                if (parts.length >= 4) {
                    String username = parts[0].trim();
                    String roleAssignment = parts[1].replace("{", "").replace("}", "").trim(); // Role Assignments
                    String permissionAssignments = parts[2].replace("{", "").replace("}", "").trim(); // Permission Assignments
                    String roleConflicts = parts[3].replace("{", "").replace("}", "").trim(); // Role Conflicts

                    OtherUser newOtherUser = new OtherUser(username,roleAssignment,permissionAssignments,roleConflicts);

                    DataMap.put(num++,newOtherUser);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Kiểm tra xung đột lợi ích
        checkConflicts(resultArea);
    }

    private static boolean isValidXacmlFile(String filePath) {
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
    private static void checkConflicts(JTextArea resultArea) {
        Map<Integer, user> MapUsers = roleBasedAccessControl.MapUsers;
        if (DataMap.size() != MapUsers.size()) {
            resultArea.setText("❌ Wrong description of number of fields and properties !");
            return;
        }

        for (Map.Entry<Integer, user> entryMotaText : MapUsers.entrySet()) {
            Integer key = entryMotaText.getKey();
            user user1 = entryMotaText.getValue();
            OtherUser user2 = DataMap.get(key);

            if (user2 == null) {
                resultArea.setText("❌ No user found corresponding to key: " + key);
                logError(key, "key_not_found", resultArea,user1);
                return;
            }

            if (!user1.getUsername().equals(user2.username)) {
                resultArea.setText("❌ Incorrect Username Description of " + user2.username + "!");
                logError(key, "username", resultArea,user1);
                return;
            }

            if (!user1.getRole().toString().equals(user2.roleAssignment)) {
                resultArea.setText("❌ Incorrect Role Description of " + user2.username + "!");
                logError(key, "role", resultArea,user1);
                return;
            }

            if (!String.join(",", user1.getPermissions()).equals(user2.permissionAssignments)) {
                resultArea.setText("❌ Incorrect Permission Description of " + user2.username + "!");
                logError(key, "permissions", resultArea,user1);
                return;
            }

            if (!String.join(",", user1.getRoleConflicts()).equals(user2.roleConflicts)) {
                resultArea.setText("❌ Incorrect RoleConflicts Description of " + user2.username + "!");
                logError(key, "roleConflicts", resultArea,user1);
                return;
            }
        }

        resultArea.setText("✅ Assign correct data!");
    }

    private static void logError(Integer key, String fieldName, JTextArea resultArea, user user) {

        String location = user.getLocation(); // Lấy vị trí tạo User

        resultArea.append("\n📍 Error in User: " + ++key);
        resultArea.append("\n🔑 Error Field: " + fieldName);
        resultArea.append("\n📝 Error Position: " + location);
//        System.out.println("📍 Lỗi xảy ra tại User thứ " + key);
//        System.out.println("🔑 Trường lỗi: " + fieldName);
//        System.out.println("📝 Vị trí lỗi: " + location);
    }

    public static void main(String[] args) {

        new SwingApp();
    }
}
