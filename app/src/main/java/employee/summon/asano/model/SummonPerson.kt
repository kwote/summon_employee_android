package employee.summon.asano.model

class SummonPerson(id: Int, firstname: String, lastname: String, patronymic: String?,
                   post: String?, email: String?, phone: String?, val inactive: Long?) : Person(id, firstname, lastname, patronymic, post, email, phone)
