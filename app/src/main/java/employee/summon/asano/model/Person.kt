package employee.summon.asano.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Person(val id: Int, val firstname: String, val lastname: String, val patronymic: String?,
             val post: String?, val email: String?, val phone: String?) : Parcelable
