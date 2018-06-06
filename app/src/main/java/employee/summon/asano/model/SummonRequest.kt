package employee.summon.asano.model

import android.os.Parcelable
import com.squareup.moshi.Json
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
                         val requested: String, val responded: String? = null,
                         val state: Int = 0, val enabled: Boolean = false,
                         val caller: Person? = null, val target: Person? = null) : Parcelable {

    val pending
        get() = state == RequestStatus.Pending.code
}