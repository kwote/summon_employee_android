package employee.summon.asano.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Person(val id: Int, @field:SerializedName("firstname") val firstName: String,
             @field:SerializedName("lastname") val lastName: String, val patronymic: String?,
             val post: String?, val email: String?, val phone: String?, val online: Boolean
) : Parcelable {
    val fullName: String
        get() = firstName + (if (patronymic != null) " $patronymic" else "") + " $lastName"
}
