package employee.summon.asano.model

import android.os.Parcelable
import employee.summon.asano.getDateWithServerTimeStamp
import kotlinx.android.parcel.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class SummonRequest(val id: Int?, val callerId: Int, val targetId: Int,
                         private val requestTime: String, private val responseTime: String?,
                         private val status: Int = 0, val enabled: Boolean = false) : Parcelable {
    val caller: String
        get() = callerId.toString()
    val target: String
        get() = targetId.toString()
    val requested: Date?
        get() = requestTime.getDateWithServerTimeStamp()

    val responded: Date?
        get() = responseTime?.getDateWithServerTimeStamp()

    val pending: Boolean
        get() = status == 0
}