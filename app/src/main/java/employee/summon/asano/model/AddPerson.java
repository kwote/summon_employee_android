package employee.summon.asano.model;

public class AddPerson extends PersonBase {
    private Integer departmentId;

    public AddPerson(String firstName, String lastName, String email, String password, Integer departmentId) {
        super(firstName, lastName, email, password);
        this.departmentId = departmentId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }
}
