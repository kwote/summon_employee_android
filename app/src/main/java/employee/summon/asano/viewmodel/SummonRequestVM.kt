package employee.summon.asano.viewmodel

import android.text.format.DateUtils
import employee.summon.asano.getDateWithServerTimeStamp
import employee.summon.asano.model.SummonRequest

class SummonRequestVM(var req: SummonRequest, var incoming: Boolean) {
    fun pending() = request.pending
    fun enabled() = request.enabled
    fun canRespond() = incoming && request.pending && request.enabled
    fun canDisable() = !incoming && request.enabled
    private fun person(): PersonVM? {
        val person = if (incoming) request.caller else request.target
        if (person != null)
            return PersonVM(person)
        return null
    }
    var request: SummonRequest
        get() = req
        set (value) {
            req = value
            person = person()
        }
    var person: PersonVM? = person()

    fun requested() = request.requested.getDateWithServerTimeStamp()?.time?.let {
        DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
    }

    fun responded() = request.responded?.getDateWithServerTimeStamp()?.time?.let {
        DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
    }

    fun accepted() = request.state == SummonRequest.RequestState.Accepted.code
    fun rejected() = request.state == SummonRequest.RequestState.Rejected.code

    fun background() =
            if (enabled()) {
                when {
                    accepted() -> android.R.color.holo_green_light
                    rejected() -> android.R.color.holo_red_light
                    else -> android.R.color.white
                }
            } else android.R.color.darker_gray
}