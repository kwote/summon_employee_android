package employee.summon.asano.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Employee(val id: Int, val post: String?, val person: Person) : Parcelable
