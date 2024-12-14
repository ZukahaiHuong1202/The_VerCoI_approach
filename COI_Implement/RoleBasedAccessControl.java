package COI_Implement;
import org.w3c.dom.ls.LSOutput;

import java.util.*;


enum role {
    TELLER, LOAN_OFFICER, AUDITOR
}

class User {

    private String username;
    private COI_Implement.role role;
    private Set<String> permissions;

    private Set<String> roleConflicts;

    public User(String username, COI_Implement.role role, Set<String> permissions, Set<String> roleConflicts) {
        this.username = username;
        this.role = role;
        this.permissions = permissions;
        this.roleConflicts = roleConflicts;
    }

    public String getUsername() {
        return username;
    }

    public COI_Implement.role getRole() {
        return role;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Set<String> getRoleConflicts() {
        return roleConflicts;
    }

}

class AccessControl {
    private final Map<role, Set<String>> rolePermissions = new HashMap<>();

    public AccessControl() {
        rolePermissions.put(role.TELLER, new HashSet<>());
        rolePermissions.put(role.LOAN_OFFICER, new HashSet<>());
        rolePermissions.put(role.AUDITOR, new HashSet<>());

        // Define permissions
        rolePermissions.get(role.TELLER).add("PROCESS");//test
        rolePermissions.get(role.TELLER).add("VIEW");//test

        rolePermissions.get(role.LOAN_OFFICER).add("CREATE_LOAN");
        //rolePermissions.get(role.LOAN_OFFICER).add("APPROVE_LOAN"); // Đây là xung đột lợi ích

        rolePermissions.get(role.AUDITOR).add("AUDIT_TRANSACTIONS");
    }

    public Set<String> getPermissionsByRole(role role) {
        return rolePermissions.get(role);
    }
}

class RoleConflicts {

    private final Map<role, Set<String>> roleConflicts = new HashMap<>();
    public  RoleConflicts() {

        roleConflicts.put(role.TELLER, new HashSet<>());
        roleConflicts.put(role.LOAN_OFFICER, new HashSet<>());
        roleConflicts.put(role.AUDITOR, new HashSet<>());

        roleConflicts.get(role.TELLER).add("CREATE_LOAN");
        roleConflicts.get(role.TELLER).add("AUDIT_TRANSACTIONS");
        roleConflicts.get(role.LOAN_OFFICER).add("AUDIT_TRANSACTIONS");
        roleConflicts.get(role.AUDITOR).add("PROCESS_CASH");
    }

    public Set<String> getRoleConflicts(role role) {
        return roleConflicts.get(role);
    }
}

public class RoleBasedAccessControl {


        AccessControl ac = new AccessControl();
        RoleConflicts rc = new RoleConflicts();

        User loanOfficer = new User("loan_officer", role.LOAN_OFFICER, ac.getPermissionsByRole(role.LOAN_OFFICER), rc.getRoleConflicts(role.LOAN_OFFICER));

        User CONG = new User("CONG", role.TELLER, ac.getPermissionsByRole(role.TELLER), rc.getRoleConflicts(role.TELLER));
        User Huong = new User("Huong", role.AUDITOR, ac.getPermissionsByRole(role.AUDITOR), rc.getRoleConflicts(role.AUDITOR));
        Map<Integer, User> MapUsers = new LinkedHashMap<>();
        static Integer num = 0;
        RoleBasedAccessControl() {
            MapUsers.put(num++,CONG);
            MapUsers.put(num++,loanOfficer);
            MapUsers.put(num++,Huong);
        }

}