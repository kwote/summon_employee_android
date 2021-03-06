package employee.summon.asano.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SummonRequest(val id: Int, val callerId: Int, val targetId: Int,
                         val requested: String, val responded: String? = null,
                         val state: Int = 0, val enabled: Boolean = false,
                         val caller: Person? = null, val target: Person? = null,
                         val comment: String? = null) : Parcelable {
    fun acceptedRequest() = copy(state = RequestState.Accepted.code)
    fun rejectedRequest() = copy(state = RequestState.Rejected.code)
    fun canceledRequest() = copy(enabled = false)

    enum class RequestState(val code: Int) {
        Pending(0),
        Accepted(1),
        Rejected(2)
    }
    val pending
        get() = state == RequestState.Pending.code
}