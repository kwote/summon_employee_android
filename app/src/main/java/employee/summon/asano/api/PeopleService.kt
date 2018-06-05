package employee.summon.asano.api

import employee.summon.asano.model.Person
import employee.summon.asano.rest.IPeopleService
import io.reactivex.Maybe

class PeopleService(private val service: IPeopleService) {
    fun listPeople(accessToken: String): Maybe<List<Person>> {
        return Maybe.create {
            subscriber ->
            val call = service.listPeople(accessToken)
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val people = response.body()
                    subscriber.onSuccess(people)
                }
                subscriber.onComplete()
            } catch (t: Throwable) {
                subscriber.onError(t)
            }
        }
    }
}