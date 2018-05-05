package employee.summon.asano.model

import android.os.Parcelable
import employee.summon.asano.getDateWithServerTimeStamp
import kotlinx.android.parcel.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class SummonRequest(val id: Int?, private val callerId: Int, val targetId: Int,
                         private val requestTime: String, private val responseTime: String?,
                         private val status: Int = 0,
                         private val enabled: Boolean = false) : Parcelable {
    fun caller(): String {
        return callerId.toString()
    }
    fun target(): String {
        return targetId.toString()
    }
    fun requested(): Date? {
        return requestTime.getDateWithServerTimeStamp()
    }
    fun responded(): Date? {
        return responseTime?.getDateWithServerTimeStamp()
    }
}