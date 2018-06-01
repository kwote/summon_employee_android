package employee.summon.asano.viewmodel

import android.text.format.DateUtils
import employee.summon.asano.model.Person
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequest

data class SummonRequestVM(val request: SummonRequest, val person: Person, val incoming: Boolean) {
    val enabled: Boolean
        get() = request.enabled
    val personName: String
        get() = person.fullName
    val requested: CharSequence
        get() = DateUtils.getRelativeTimeSpanString(request.requested!!.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
    val responded: CharSequence?
        get() = request.responded?.time?.let { DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS) }
    val accepted: Boolean
        get() = request.state == RequestStatus.Accepted.code
    val rejected: Boolean
        get() = request.state == RequestStatus.Rejected.code
}