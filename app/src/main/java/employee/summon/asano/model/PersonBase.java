package employee.summon.asano.model;

import com.google.gson.annotations.SerializedName;

public class PersonBase {
    private Integer departmentId;
    private boolean online;
    public PersonBase(String firstName, String lastName, String email, String password, Integer departmentId, boolean online) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.departmentId = departmentId;
        this.online = online;
    }

    @SerializedName("firstname")
    private String firstName;

    public String getFirstName() {
        return firstName;
    }

    @SerializedName("lastname")
    private String lastName;

    public String getLastName() {
        return lastName;
    }

    private String email;
    public String getEmail() {
        return email;
    }

    private String password;

    public String getPassword() {
        return password;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public boolean isOnline() {
        return online;
    }
}
