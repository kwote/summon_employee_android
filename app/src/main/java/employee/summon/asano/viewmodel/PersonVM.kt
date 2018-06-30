package employee.summon.asano.viewmodel

import android.content.Context
import android.text.TextUtils
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.model.Person

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
}
