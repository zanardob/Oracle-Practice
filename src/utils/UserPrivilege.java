package utils;

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

    public void setUser(String user) {
        this.user = user;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }
}
