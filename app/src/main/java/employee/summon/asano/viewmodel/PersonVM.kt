package employee.summon.asano.viewmodel

import android.content.Context
import android.text.TextUtils
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.RequestListenerService
import employee.summon.asano.getDateWithServerTimeStamp
import employee.summon.asano.model.Person
import java.util.*

class PersonVM(var person: Person) {
    fun fullName() =
            person.firstname +
                    (if (!TextUtils.isEmpty(person.patronymic))
                        " ${person.patronymic}"
                    else
                        "") +
                    " ${person.lastname}"

    fun post() = person.post
    fun email() = person.email
    fun phone() = person.phone
    fun canDial() = !TextUtils.isEmpty(person.phone)
    fun isMe(context: Context): Boolean = App.getApp(context).user.id == person.id
    fun fullNameOrMe(context: Context): CharSequence? =
            if (isMe(context)) {
                context.getString(R.string.me)
            } else fullName()

    fun online(context: Context): String {
        val lastActive = person.lastActiveTime?.getDateWithServerTimeStamp()
        lastActive?.let {
            val minusPing = Calendar.getInstance()
            minusPing.add(Calendar.SECOND, (-RequestListenerService.PING_PERIOD).toInt())
            val minus = minusPing.time
            return if (minus.before(lastActive)) {
                context.getString(R.string.online)
            } else {
                context.getString(R.string.offline)
            }
        }
        return context.getString(R.string.offline)
    }
}
