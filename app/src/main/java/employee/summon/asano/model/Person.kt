package employee.summon.asano.model

import android.os.Parcelable
import android.text.TextUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
class Person(val id: Int, val firstname: String, val lastname: String, val patronymic: String?,
             val post: String?, val email: String?, val phone: String?, val lastActiveTime: String?
) : Parcelable {
    val fullName
        get() = firstname + (if (!TextUtils.isEmpty(patronymic)) " $patronymic" else "") + " $lastname"
}
