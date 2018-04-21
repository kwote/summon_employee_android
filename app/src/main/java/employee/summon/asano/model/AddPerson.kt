package employee.summon.asano.model

class AddPerson(firstName: String, lastName: String, email: String, val password: String,
                departmentId: Int?, online: Boolean) : PersonBase(firstName, lastName, email, departmentId, online)