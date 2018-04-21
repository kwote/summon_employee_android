package employee.summon.asano.model;

public class Person extends PersonBase {
    private Integer id;

    public Person(Integer id, String firstName, String lastName, String email, String password, Integer departmentId, boolean online) {
        super(firstName, lastName, email, password, departmentId, online);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
