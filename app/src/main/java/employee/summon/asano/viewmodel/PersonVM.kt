package employee.summon.asano.viewmodel

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.RequestListenerService
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonPerson

class PersonVM(var person: Person) {
    fun fullName() =
            person.firstname +
                    (if (!TextUtils.isEmpty(person.patronymic))
                        " ${person.patronymic}"
                    else
                        "") +
                    " ${person.lastname}"

    val post = person.post
    val email = person.email
    val phone = person.phone
    fun canDial() = !TextUtils.isEmpty(person.phone)
    fun isMe(context: Context): Boolean = App.getApp(context).user.id == person.id
    fun fullNameOrMe(context: Context): CharSequence? =
            if (isMe(context)) {
                context.getString(R.string.me)
            } else fullName()

    fun online(context: Context): CharSequence? {
        val inactive = (person as SummonPerson).inactive
        inactive?.let {
            val seconds = inactive / 1000
            return if (seconds <= RequestListenerService.PING_PERIOD) {
                context.getString(R.string.online)
            } else {
                val now = System.currentTimeMillis()
                DateUtils.getRelativeTimeSpanString(now - inactive, now, DateUtils.MINUTE_IN_MILLIS)
            }
        }
        return context.getString(R.string.offline)
    }
}
