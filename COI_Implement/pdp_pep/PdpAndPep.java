package COI_Implement.pdp_pep;

import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PdpAndPep {
    public static void CheckPdpAndPep(JTextArea resultArea, String filePath){

        // Khởi tạo PDP dựa trên file XACML
        PdpService pdpService = new PdpService(resultArea,filePath);
        PepService pepService = new PepService(resultArea,pdpService);

        // Test xem role có đúng với quyền hạn hay ko
        pdpService.getPolicyRules().remove("ITStaff:SystemLogs:Write");
        pdpService.changeRule("Physician:MedicalRecord:Write", "Deny");
        pdpService.getPolicyRules().put("Admin", "Permit");

        PolicyVerifier.verifyPolicy(resultArea,pdpService, filePath);

        // Test xem PDP có đúng với XACML hay ko
        pepService.requestAccess(resultArea,"Receptionist", "PatientInfo", "Write");  // Hợp lệ
        pepService.requestAccess(resultArea,"Intern", "MedicalRecord", "Write"); // Bị từ chối
        pepService.requestAccess(resultArea,"UnknownRole", "MedicalRecord", "Read"); // Role không tồn tại
    }

    static class PdpService {
        private final Map<String, String> policyRules;

        public PdpService(JTextArea resultArea, String policyFilePath) {
            this.policyRules = XacmlPolicyLoader.loadPolicy(resultArea,policyFilePath);
        }

        public void changeRule(String changeRole, String newEffect) {
            Map<String, String> updatedRules = new HashMap<>(policyRules);
            updatedRules.forEach((key, value) -> {
                if (key.startsWith(changeRole)) {
                    policyRules.put(key, newEffect);
                }
            });
        }

        public String evaluateAccess(String role) {
            return policyRules.getOrDefault(role, "Deny");
        }

        public boolean hasRole(String role) {
            return policyRules.containsKey(role);
        }

        public Map<String, String> getPolicyRules() {
            return policyRules;
        }
    }

    static class PepService {
        private final PdpService pdpService;

        public PepService(JTextArea resultArea, PdpService pdpService) {
            this.pdpService = pdpService;
        }

        public void requestAccess(JTextArea resultArea,String role, String resource, String action) {
            String key = role + ":" + resource + ":" + action;
            String decision = pdpService.evaluateAccess(key);

            if ("Permit".equalsIgnoreCase(decision)) {
                resultArea.append("✅ PERMITTED ACCESS CHO: Role=" + role + ", Resource=" + resource + ", Action=" + action +"\n");
            } else {
                String reason = !pdpService.hasRole(key) ? "Role does not exist in XACML" : "XACML policy denies access";
                resultArea.append("❌ ACCESS DENIED: Role=" + role + ", Resource=" + resource + ", Action=" + action + ". Reason: " + reason +"\n");
            }
        }
    }

    static class PolicyVerifier {
        public static void verifyPolicy(JTextArea resultArea, PdpService pdpService, String xacmlFilePath) {
            Map<String, String> xacmlPolicies = XacmlPolicyLoader.loadPolicy(resultArea,xacmlFilePath);
            Map<String, String> pdpPolicies = pdpService.getPolicyRules();
            boolean isValid = true;

            for (String role : xacmlPolicies.keySet()) {
                if (!pdpPolicies.containsKey(role)) {
                    resultArea.append("⚠️ ERROR: PDP lacks policy for role: " + role +"\n");
                    isValid = false;
                } else if (!pdpPolicies.get(role).equals(xacmlPolicies.get(role))) {
                    resultArea.append("❌ ERROR: PDP has incorrect information for role: " + role +"\n");
                    isValid = false;
                }
            }

            for (String role : pdpPolicies.keySet()) {
                if (!xacmlPolicies.containsKey(role)) {
                    resultArea.append("⚠️ ERROR: PDP has an extra role not present in XACML: " + role +"\n");
                    isValid = false;
                }
            }

            if (isValid) {
                resultArea.append("✅ PDP COMPLETELY MATCHES XACML!" +"\n");
            } else {
                resultArea.append("❌ PDP has error and is not consistent with XACML. Please check the PDP installation AGAIN!" +"\n");
            }
        }
    }

    static class XacmlPolicyLoader {
        public static Map<String, String> loadPolicy(JTextArea resultArea,String filePath) {
            Map<String, String> policyRules = new HashMap<>();
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    resultArea.append("ERROR: XACML file does not exist: " + filePath +"\n");
                    return policyRules;
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                document.getDocumentElement().normalize();

                NodeList ruleNodes = document.getElementsByTagName("Rule");

                for (int i = 0; i < ruleNodes.getLength(); i++) {
                    Element ruleElement = (Element) ruleNodes.item(i);
                    String effect = ruleElement.getAttribute("Effect");

                    String role = "", resource = "", action = "";
                    NodeList matchNodes = ruleElement.getElementsByTagName("Match");
                    for (int j = 0; j < matchNodes.getLength(); j++) {
                        Element matchElement = (Element) matchNodes.item(j);
                        NodeList valueNodes = matchElement.getElementsByTagName("AttributeValue");
                        NodeList designatorNodes = matchElement.getElementsByTagName("AttributeDesignator");

                        if (valueNodes.getLength() > 0 && designatorNodes.getLength() > 0) {
                            String attributeValue = valueNodes.item(0).getTextContent().trim();
                            String attributeId = ((Element) designatorNodes.item(0)).getAttribute("AttributeId");

                            if (!attributeValue.isEmpty() && !attributeId.isEmpty()) {
                                if (attributeId.endsWith("role")) role = attributeValue;
                                else if (attributeId.endsWith("resource")) resource = attributeValue;
                                else if (attributeId.endsWith("action")) action = attributeValue;
                            }
                        }
                    }
                    if (!role.isEmpty() && !resource.isEmpty() && !action.isEmpty()) {
                        policyRules.put(role + ":" + resource + ":" + action, effect);
                    }
                }
            } catch (Exception e) {
                resultArea.append("ERROR: Unable to load XACML file: " + e.getMessage() +"\n");
            }
            return policyRules;
        }
    }

}
