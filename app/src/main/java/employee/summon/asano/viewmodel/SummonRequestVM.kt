package employee.summon.asano.viewmodel

import android.text.format.DateUtils
import employee.summon.asano.getDateWithServerTimeStamp
import employee.summon.asano.model.SummonRequest

data class SummonRequestVM(val request: SummonRequest, val incoming: Boolean) {
    val pending = request.pending
    val enabled = request.enabled
    val person : PersonVM?
        get() {
            val person = if (incoming) request.caller else request.target
            if (person != null)
                return PersonVM(person)
            return null
        }
    val requested = request.requested.getDateWithServerTimeStamp()?.time?.let {
            DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
    val responded = request.responded?.getDateWithServerTimeStamp()?.time?.let {
            DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
    val accepted = request.state == SummonRequest.RequestStatus.Accepted.code
    val rejected = request.state == SummonRequest.RequestStatus.Rejected.code
}