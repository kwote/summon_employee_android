package employee.summon.asano

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/** Converting from Date to String**/
fun Date.getStringTimeStampWithDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormat.format(this)
}

/** Converting from String to Date **/
fun String.getDateWithServerTimeStamp(): Date? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")  // IMP !!!
    return try {
        dateFormat.parse(this)
    } catch (e: ParseException) {
        null
    }
}