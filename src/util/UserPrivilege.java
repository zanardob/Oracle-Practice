package util;

public class UserPrivilege {
    private String user;
    private String privilege;

    public UserPrivilege(String user, String privilege) {
        this.user = user;
        this.privilege = privilege;
    }

    public String getUser() {
        return user;
    }

    public String getPrivilege() {
        return privilege;
    }
}
