package employee.summon.asano.viewmodel

import android.content.Context
import android.text.TextUtils
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.model.Person

class PersonVM(var person: Person) {
    val fullName
        get() = person.firstname +
                (if (!TextUtils.isEmpty(person.patronymic)) " ${person.patronymic}" else "") +
                " ${person.lastname}"
    val post = person.post
    val email = person.email
    val phone = person.phone
    fun fullNameOrMe(context: Context): CharSequence? =
        if (App.getApp(context).user.id == person.id) {
            context.getString(R.string.me)
        } else fullName
}
