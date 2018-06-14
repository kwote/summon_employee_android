package employee.summon.asano.model

data class AddPerson(val firstname: String, val lastname: String, val patronymic: String?,
                val post: String?, val email: String, val phone: String?, val password: String,
                val lastActiveTime: String)