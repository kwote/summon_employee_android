package employee.summon.asano.viewmodel

import android.text.format.DateUtils
import employee.summon.asano.getDateWithServerTimeStamp
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequest

data class SummonRequestVM(val request: SummonRequest, val incoming: Boolean) {
    val enabled
        get() = request.enabled
    val person
        get() = if (incoming) request.caller else request.target
    val personName
        get() = person?.fullName
    val requested: CharSequence?
        get() = request.requested.getDateWithServerTimeStamp()?.time?.let {
            DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
    val responded: CharSequence?
        get() = request.responded?.getDateWithServerTimeStamp()?.time?.let {
            DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
    val accepted
        get() = request.state == RequestStatus.Accepted.code
    val rejected
        get() = request.state == RequestStatus.Rejected.code
}