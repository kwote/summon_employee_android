package employee.summon.asano.viewmodel

import employee.summon.asano.getDateWithServerTimeStamp
import java.util.*

data class SummonRequestVM(val id: Int?, private val requestTime: String, private val responseTime: String?,
                           private val incoming: Boolean, val personName: String,
                           private val status: Int = 0, val enabled: Boolean = false) {
    fun requested(): Date? {
        return requestTime.getDateWithServerTimeStamp()
    }
    fun responded(): Date? {
        return responseTime?.getDateWithServerTimeStamp()
    }
}