package employee.summon.asano.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import employee.summon.asano.getDateWithServerTimeStamp
import kotlinx.android.parcel.Parcelize
import java.util.*

enum class RequestStatus(val code: Int) {
    Pending(0),
    Accepted(1),
    Rejected(2)
}

@Parcelize
data class SummonRequest(val id: Int?, val callerId: Int, val targetId: Int,
                         @field:SerializedName("requested") private val requestTime: String,
                         @field:SerializedName("responded") private val responseTime: String? = null,
                         val state: Int = 0, val enabled: Boolean = false) : Parcelable {
    val requested
        get() = requestTime.getDateWithServerTimeStamp()

    val responded
        get() = responseTime?.getDateWithServerTimeStamp()

    val pending
        get() = state == RequestStatus.Pending.code
}