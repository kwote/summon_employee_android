package employee.summon.asano.model

import com.squareup.moshi.Json

class AddPerson(@Json(name = "firstname") val firstName: String,
                @Json(name = "lastname") val lastName: String, val patronymic: String?,
                val post: String?, val email: String, val phone: String?, val password: String,
                val lastActiveTime: String
)