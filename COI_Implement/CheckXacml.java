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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckXacml {
    public static void checkNguoiDungList(List<NguoiDung> list, JTextArea resultCheckXacmlArea) {
        for (NguoiDung user : list) {
            String ruleId = user.getRuleId();
            if (user.getRole() == null || user.getRole().isEmpty()) {
                resultCheckXacmlArea.append("Lack of information: Role for Rule " + ruleId + "\n");
                //System.out.println("Thiếu thông tin: role cho rule " + ruleId);
            }
            if (user.getRoleType() == null || user.getRoleType().isEmpty()) {
                resultCheckXacmlArea.append("Lack of information: roleType for Rule " + ruleId + "\n");
                //System.out.println("Thiếu thông tin: roleType cho rule " + ruleId);
            }
            if (user.getResource() == null || user.getResource().isEmpty()) {
                resultCheckXacmlArea.append("Lack of information: Resource for Rule " + ruleId + "\n");
                //System.out.println("Thiếu thông tin: resource cho rule " + ruleId);
            }
            if (user.getActions() == null || user.getActions().length == 0) {
                resultCheckXacmlArea.append("Lack of information: Actions for Rule " + ruleId + "\n");
                //System.out.println("Thiếu thông tin: actions cho rule " + ruleId);
            }
            if (user.getEffect() == null || user.getEffect().isEmpty()) {
                resultCheckXacmlArea.append("Lack of information: Effect for Rule "+ ruleId + "\n");
                //System.out.println("Thiếu thông tin: effect cho rule " + ruleId);
            }
        }
    }

    public static void checkFileForConflicts(JTextArea resultCheckXacmlArea, String xacmlFilePath) throws IOException, ParserConfigurationException, SAXException {
        if (xacmlFilePath == null || xacmlFilePath.isEmpty()) {
            resultCheckXacmlArea.setText("ERROR: You have not selected XACML File!");
            return;
        }

        if (!xacmlFilePath.toLowerCase().endsWith(".xacml") && !xacmlFilePath.toLowerCase().endsWith(".xml")) {
            resultCheckXacmlArea.setText("ERROR: File is not XACML's format, select again please!");
            return;
        }
        final List<NguoiDung> userList = new ArrayList<>();

        // Đọc file XACML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xacmlFilePath);
        document.getDocumentElement().normalize();

        NodeList ruleNodes = document.getElementsByTagName("Rule");
        for (int j = 0; j < ruleNodes.getLength(); j++) {
            Map<String, String[]> tmp = new LinkedHashMap<>();
            Element ruleNode = (Element) ruleNodes.item(j);
            String RuleId = ruleNode.getAttribute("RuleId");
            String Effect = ruleNode.getAttribute("Effect");
            if (Effect.trim().isEmpty()) {
                resultCheckXacmlArea.setText("\nRuleID: " + RuleId + " is missing field effect.\n");
            }

            tmp.put("RuleID", new String[]{RuleId});
            tmp.put("Effect", new String[]{Effect});

            NodeList Matchs = ruleNode.getElementsByTagName("Match");
            for (int k = 0; k < Matchs.getLength(); k++) {
                Element Match = (Element) Matchs.item(k);
                String[] ctg = ((Element) Match.getElementsByTagName("AttributeDesignator").item(0)).getAttribute("Category").split(":");
                String Category = ctg[ctg.length - 1];
                String value = ((Element) Match.getElementsByTagName("AttributeDesignator").item(0)).getAttribute("AttributeId");

                if (Match.getElementsByTagName("AttributeValue").item(0).getTextContent().trim().isEmpty()) {
                    continue;
                }

                if (Category.equals("action") && tmp.containsKey("action")) {
                    String[] existingActions = tmp.get(Category);
                    String[] newActions = Arrays.copyOf(existingActions, existingActions.length + 1);
                    newActions[newActions.length - 1] = Match.getElementsByTagName("AttributeValue").item(0).getTextContent();
                    tmp.put("action", newActions);
                } else if (Category.equals("access-subject")){
                    tmp.put("role", new String[]{Match.getElementsByTagName("AttributeValue").item(0).getTextContent()});
                    tmp.put("roleType", new String[]{value});
                }
                else {
                    tmp.put(Category, new String[]{Match.getElementsByTagName("AttributeValue").item(0).getTextContent()});
                }
            }
            try {
                userList.add(new NguoiDung(tmp.get("role")[0], tmp.get("roleType")[0], tmp.get("resource")[0], tmp.get("action"), tmp.get("RuleID")[0], tmp.get("Effect")[0]));
            } catch (Exception e) {
                //resultCheckXacmlArea.append("Error: " + e.getMessage());
                String[] fields = new String[]{"role", "roleType", "resource", "action", "Effect"};
                String Id = tmp.get("RuleID")[0];
                for (int i = 0; i < fields.length - 1; i++) {
                    String field = fields[i];
                    boolean check = tmp.containsKey(fields[i]);

                    if (!check) {
                        tmp.put(fields[i], new String[]{""});
                    }
                }
                userList.add(new NguoiDung(tmp.get("role")[0], tmp.get("roleType")[0], tmp.get("resource")[0], tmp.get("action"), tmp.get("RuleID")[0], tmp.get("Effect")[0]));
            }
        }

        checkNguoiDungList(userList, resultCheckXacmlArea);

        Map<String, List<NguoiDung>> groupedUsers = userList.stream()
                .collect(Collectors.groupingBy(user -> user.getRole() + "-" + user.getRoleType() + "-" + user.getResource() + "-" + String.join(", ",user.getActions()) + "-" + user.getEffect()));

        List<Map.Entry<String, List<NguoiDung>>> filteredGroups = groupedUsers.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .toList();

        if (filteredGroups.isEmpty()) {
            resultCheckXacmlArea.append("Rule have not duplicated.\n");
        } else {
            filteredGroups.forEach(entry -> {
                resultCheckXacmlArea.append("Rule duplicated: " + entry.getKey()+"\n");
                entry.getValue().forEach(user -> resultCheckXacmlArea.append(user.toString() + "\n"));
                //resultCheckXacmlArea.append("\n");
            });
        }

        // Nhóm user theo hậu tố sau "Permit" hoặc "Deny"
        Map<String, List<NguoiDung>> groupedUsers_ID = userList.stream()
                .filter(user -> {
                    String ruleId = user.getRuleId().trim();
                    return ruleId.startsWith("Permit") || ruleId.startsWith("Deny");
                }) // Lọc role có "Permit" hoặc "Deny"
                .collect(Collectors.groupingBy(user -> user.getRuleId().replaceFirst("^(Permit|Deny)", ""))); // Nhóm theo hậu tố

        // In kết quả
        groupedUsers_ID.forEach((suffix, users) -> {
            resultCheckXacmlArea.append("🔹 Group: " + suffix + "\n");
            users.forEach(user -> resultCheckXacmlArea.append(user.toString() + "\n"));

            String baseEffect = users.get(0).getEffect();
            users.forEach(user -> {
                if (!user.getEffect().equals(baseEffect)) {
                    resultCheckXacmlArea.append("Xung đột dải Gazza.");
                }
            });
            resultCheckXacmlArea.append("\n");
        });
    }
}
