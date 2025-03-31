package COI_Implement;

public class NguoiDung {
    private String role;
    private String roleType;
    private String resource;
    private String[] actions;
    private String ruleId;
    private String effect;

    public NguoiDung(String role, String roleType, String resource, String[] actions, String ruleId, String effect) {
        this.role = (role != null) ? role : "";
        this.roleType = (roleType != null) ? roleType : "";
        this.resource = (resource != null) ? resource : "";
        this.actions = (actions != null) ? actions : new String[]{};
        this.ruleId = (ruleId != null) ? ruleId : "";
        this.effect = (effect != null) ? effect : "";
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = (effect != null) ? effect : "";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null) ? role : "";
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = (roleType != null) ? roleType : "";
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = (resource != null) ? resource : "";
    }

    public String[] getActions() {
        return actions;
    }

    public void setActions(String[] actions) {
        this.actions = (actions != null) ? actions : new String[]{};
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = (ruleId != null) ? ruleId : "";
    }

    @Override
    public String toString() {
        return "User{" +
                "role='" + role + '\'' +
                ", roleType='" + roleType + '\'' +
                ", resource='" + resource + '\'' +
                ", actions=" + (actions.length > 0 ? String.join(", ", actions) : "[]") +
                ", ruleId='" + ruleId + '\'' +
                ", effect='" + effect + '\'' +
                '}';
    }
}
