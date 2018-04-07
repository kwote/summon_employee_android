package employee.summon.asano.model;

public class Department {

    private Integer id;
    private String name;

    public Department(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
