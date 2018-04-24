package employee.summon.asano.model

class Person(val id: Int?, firstName: String, lastName: String, patronymic: String, email: String,
             phone: String, departmentId: Int?, online: Boolean
) : PersonBase(firstName, lastName, patronymic, email, phone, departmentId, online)
