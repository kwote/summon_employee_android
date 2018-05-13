package employee.summon.asano.model

import android.os.Parcelable
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
                         private val requestTime: String, private val responseTime: String? = null,
                         val status: Int = 0, val enabled: Boolean = false) : Parcelable {
    val requested: Date?
        get() = requestTime.getDateWithServerTimeStamp()

    val responded: Date?
        get() = responseTime?.getDateWithServerTimeStamp()

    val pending: Boolean
        get() = status == RequestStatus.Pending.code
}