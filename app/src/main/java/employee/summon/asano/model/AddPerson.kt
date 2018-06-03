package employee.summon.asano.model

import com.google.gson.annotations.SerializedName

class AddPerson(@field:SerializedName("firstname") val firstName: String,
                @field:SerializedName("lastname") val lastName: String, val patronymic: String?,
                val email: String, val phone: String?, val password: String,
                val lastActiveTime: String
)