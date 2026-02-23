package src.models;

public class User {
    private String username;
    private Role role;
    private String division;

    public User(String username, Role role, String division) {
        this.username = username;
        this.role = role;
        this.division = division;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getDivision() {
        return division;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role=" + role +
                ", division='" + division + '\'' +
                '}';
    }
}
