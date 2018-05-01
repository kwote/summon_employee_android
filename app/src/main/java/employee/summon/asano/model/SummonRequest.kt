package employee.summon.asano.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class SummonRequest(val id: Int?, private val callerId: Int, val targetId: Int,
                         private val summonTime: String, private val receiveTime: String?,
                         private val acceptTime: String?) {
    fun caller(): String {
        return callerId.toString()
    }
    fun target(): String {
        return targetId.toString()
    }
    fun summoned(): Date? {
        return summonTime.getDateWithServerTimeStamp()
    }
    fun received(): Date? {
        return receiveTime?.getDateWithServerTimeStamp()
    }
    fun accepted(): Date? {
        return acceptTime?.getDateWithServerTimeStamp()
    }

    /** Converting from String to Date **/
    private fun String.getDateWithServerTimeStamp(): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")  // IMP !!!
        return try {
            dateFormat.parse(this)
        } catch (e: ParseException) {
            null
        }
    }
}