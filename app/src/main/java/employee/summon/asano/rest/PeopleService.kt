package employee.summon.asano.rest

import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.AddPerson
import employee.summon.asano.model.LoginCredentials
import employee.summon.asano.model.Person
import retrofit2.Call;
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PeopleService {
    @GET("people")
    fun listPeople(): Call<List<Person>>

    @POST("people")
    fun addPerson(@Body person: AddPerson): Call<Person>

    @POST("people/login")
    fun login(@Body credentials: LoginCredentials): Call<AccessToken>
}
