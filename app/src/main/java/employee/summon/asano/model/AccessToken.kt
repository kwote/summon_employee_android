package employee.summon.asano.model

import android.os.Parcelable
import employee.summon.asano.getDateWithServerTimeStamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class AccessToken(val id: String, val ttl: Int?, private val created: String, val userId: Int?,
                       val user: Person): Parcelable {
    fun createTime(): Date? {
        return created.getDateWithServerTimeStamp()
    }

    fun expired(): Boolean {
        val instance = Calendar.getInstance()
        val now = instance.time
        instance.time = createTime()
        ttl?.let { instance.add(Calendar.SECOND, it) }
        return instance.time.before(now)
    }
}
