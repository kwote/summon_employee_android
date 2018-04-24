package employee.summon.asano.model

class AddPerson(firstName: String, lastName: String, patronymic: String?, email: String,
                phone: String?, val password: String, departmentId: Int?, online: Boolean
) : PersonBase(firstName, lastName, patronymic, email, phone, departmentId, online)