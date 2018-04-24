package employee.summon.asano.model

import com.google.gson.annotations.SerializedName

open class PersonBase(
        @field:SerializedName("firstname") val firstName: String,
        @field:SerializedName("lastname") val lastName: String,
        val patronymic: String?, val email: String, val phone: String?,
        val departmentId: Int?, val online: Boolean)