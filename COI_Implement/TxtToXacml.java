//package COI_Implement;
//
//import org.xml.sax.SAXException;
//
//import javax.swing.*;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.*;
//import java.nio.file.*;
//import java.util.*;
//
//public class TxtToXacml {
//
//    public static void checkFileForConflicts(JTextArea resultArea, String filePath, String outputDirectory) throws IOException, ParserConfigurationException, SAXException {
//        if (!filePath.endsWith(".txt")) {
//            resultArea.setText("Lỗi: Chỉ hỗ trợ đọc file .txt!");
//            return;
//        }
//
//        File inputFile = new File(filePath);
//        String outputFileName = inputFile.getName().replace(".txt", ".xacml");
//        String outputFilePath = Paths.get(outputDirectory, outputFileName).toString();
//
//        try {
//            List<Map<String, String>> users = parseTxtFile(filePath);
//            System.out.println(users);
//            String xacmlContent = generateXacml(users);
//            Files.write(Paths.get(outputFilePath), xacmlContent.getBytes());
//            resultArea.setText("Chuyển đổi thành công! File XACML đã được tạo tại: " + outputFilePath);
//        } catch (IOException e) {
//            resultArea.setText("Lỗi khi xử lý file: " + e.getMessage());
//        }
//    }
//
//    private static List<Map<String, String>> parseTxtFile(String filePath) throws IOException {
//        List<Map<String, String>> users = new ArrayList<>();
//
//
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            boolean isHeader = true;
//            while ((line = br.readLine()) != null) {
//                if (isHeader) {
//                    isHeader = false;
//                    continue;
//                }
//                String[] columns = line.split("\\s+");
//                if (columns.length < 5) continue;
//                Map<String, String> user = new HashMap<>();
//                user.put("ruleId", columns[0]);
//                user.put("subject", columns[1].replace("role =", "").replace("group =", "").trim());
//                user.put("object", columns[2].replace("resource =", "").trim());
//                user.put("action", columns[3].trim());
//                user.put("effect", columns[4].trim().equalsIgnoreCase("Permit") ? "Permit" : "Deny");
//                users.add(user);
//            }
//        }
//        return users;
//    }
//
//    private static String generateXacml(List<Map<String, String>> users) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        sb.append("<PolicySet xmlns=\"urn:oasis:names:tc:xacml:3.0\" PolicySetId=\"AccessControlPolicy\" PolicyCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable\">\n");
//        for (Map<String, String> user : users) {
//            sb.append("  <Policy PolicyId=\"" + user.get("ruleId") + "\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\">\n");
//            sb.append("    <Target>\n");
//            sb.append("      <AnyOf>\n");
//            sb.append("        <AllOf>\n");
//            sb.append(generateMatch("subject", "urn:oasis:names:tc:xacml:1.0:subject:role", user.get("subject")));
//            sb.append(generateMatch("object", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", user.get("object")));
//            for (String act : user.get("action").split(",")) {
//                sb.append(generateMatch("action", "urn:oasis:names:tc:xacml:1.0:action:action-id", act.trim()));
//            }
//            sb.append("        </AllOf>\n");
//            sb.append("      </AnyOf>\n");
//            sb.append("    </Target>\n");
//            sb.append("    <Rule RuleId=\"" + user.get("ruleId") + "\" Effect=\"" + user.get("effect") + "\"/>\n");
//            sb.append("  </Policy>\n");
//        }
//        sb.append("</PolicySet>\n");
//        return sb.toString();
//    }
//
//    private static String generateMatch(String category, String attributeId, String value) {
//        return "          <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
//                "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + value + "</AttributeValue>\n" +
//                "            <AttributeDesignator AttributeId=\"" + attributeId + "\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:" + category + "\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
//                "          </Match>\n";
//    }
//}
package COI_Implement;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TxtToXacml {

    public static void checkFileForConflicts(JTextArea resultArea,JTextArea resultTxtToXacmlArea2, String filePath, String outputDirectory) throws IOException, ParserConfigurationException, SAXException {
        if (!filePath.endsWith(".txt")) {
            resultArea.setText("ERROR: Support \".txt\" format File only!");
            return;
        }

        File inputFile = new File(filePath);
        String outputFileName = inputFile.getName().replace(".txt", ".xacml");
        String outputFilePath = Paths.get(outputDirectory, outputFileName).toString();

        try {
            List<Map<String, String[]>> users = parseTxtFile(filePath);
            System.out.println(users.toString());
            String xacmlContent = generateXacml(users);
            Files.write(Paths.get(outputFilePath), xacmlContent.getBytes());
            resultArea.setText("File XACML has been created at: " + outputFilePath +"\n");
            resultArea.append("Convert Success!");
            resultTxtToXacmlArea2.append(xacmlContent);
        } catch (IOException e) {
            resultArea.setText("Error processing file: " + e.getMessage());
        }
    }

    private static List<Map<String, String[]>> parseTxtFile(String filePath) throws IOException {
        List<Map<String, String[]>> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split("\\s+");
                if (columns.length < 5) continue;
                Map<String, String[]> user = new HashMap<>();
                user.put("ruleId", Arrays.copyOfRange(columns, 0, 1));
                user.put("subject", Arrays.copyOfRange(columns, 1, 4));
                user.put("object", Arrays.copyOfRange(columns, 4, 7));
                user.put("action", Arrays.copyOfRange(columns, 7, 9));
                user.put("effect", Arrays.copyOfRange(columns, 9, 10));
                users.add(user);
            }
        }
        return users;
    }

    private static String generateXacml(List<Map<String, String[]>> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        sb.append("<Policy\r\n" + //
//                "    xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"\r\n" + //
//                "    PolicyId=\"HealthcareAccessPolicy\"\r\n" + //
//                "    RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides\">\r\n" + //
//                "  \r\n" + //
//                "  <Description>Access control for healthcare-related resources</Description>\n");
        sb.append("<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0\" PolicyId=\"AccessControlPolicy\" PolicyCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable\">\n");
        for (Map<String, String[]> user : users) {
            sb.append("  <Rule RuleId=\"" + user.get("ruleId")[0] + "\" Effect=\"" + user.get("effect")[0] + "\">\n");

            sb.append("    <Target>\n");
            sb.append("      <AnyOf>\n");
            sb.append("        <AllOf>\n");
            sb.append(generateMatch("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", user.get("subject")[0], user.get("subject")[user.get("subject").length - 1]));
            sb.append(generateMatch("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", user.get("object")[0], user.get("object")[user.get("object").length - 1]));
            for (String act : user.get("action")) {
                sb.append(generateMatch("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "action", act.replace(",", "")));
            }
            sb.append("        </AllOf>\n");
            sb.append("      </AnyOf>\n");
            sb.append("    </Target>\n");
            sb.append("  </Rule>\n");
        }
        sb.append("</Policy>\n");
        return sb.toString();
    }

    private static String generateMatch(String category, String attributeId, String value) {
        return "          <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + value + "</AttributeValue>\n" +
                "            <AttributeDesignator" +
                "                Category=\"" + category + "\"" +
                "                AttributeId=\"" + attributeId + "\"" +
                "                DataType=\"http://www.w3.org/2001/XMLSchema#string\"" +
                "                MustBePresent=\"false\"/>" +
                "          </Match>\n";
    }
}
