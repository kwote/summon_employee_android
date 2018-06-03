package employee.summon.asano.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Person(val id: Int, @field:SerializedName("firstname") val firstName: String,
             @field:SerializedName("lastname") val lastName: String, val patronymic: String?,
             val email: String?, val phone: String?, val lastActiveTime: String?
) : Parcelable {
    val fullName: String
        get() = firstName + (if (patronymic != null) " $patronymic" else "") + " $lastName"
}
