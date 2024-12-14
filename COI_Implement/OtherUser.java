package COI_Implement;

public class OtherUser {
    String username;
    String roleAssignment;
    String permissionAssignments;
    String roleConflicts;

    public OtherUser(String username, String roleAssignment, String permissionAssignments, String roleConflicts) {
        this.username = username;
        this.roleAssignment = roleAssignment;
        this.permissionAssignments = permissionAssignments;
        this.roleConflicts = roleConflicts;
    }
}
