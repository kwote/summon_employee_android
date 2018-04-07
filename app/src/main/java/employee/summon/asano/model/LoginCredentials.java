package employee.summon.asano.model;

public class LoginCredentials {
    public LoginCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    private String email;

    public String getEmail() {
        return email;
    }

    private String password;

    public String getPassword() {
        return password;
    }
}
