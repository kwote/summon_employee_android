package employee.summon.asano.model;

import com.google.gson.annotations.SerializedName;

public class Person {
    public Person(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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
}
