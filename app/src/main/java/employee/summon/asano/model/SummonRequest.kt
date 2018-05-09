package employee.summon.asano.model

import android.os.Parcelable
import employee.summon.asano.getDateWithServerTimeStamp
import kotlinx.android.parcel.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class SummonRequest(val id: Int?, val callerId: Int, val targetId: Int,
                         val requestTime: String, val responseTime: String?,
                         val status: Int = 0,
                         val enabled: Boolean = false) : Parcelable {
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