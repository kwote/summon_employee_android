package employee.summon.asano.rest

import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.AddPerson
import employee.summon.asano.model.LoginCredentials
import employee.summon.asano.model.Person
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PeopleService {
    @GET("people")
    fun listPeople(@Query("departmentId") departmentId: Int?): Call<List<Person>>

    @POST("people")
    fun addPerson(@Body person: AddPerson): Call<Person>

    @POST("people/login")
    fun login(@Body credentials: LoginCredentials): Call<AccessToken>

    @POST("people/logout")
    fun logout(@Query("access_token") accessToken: String): Call<ResponseBody>

    @DELETE("people/{id}/accessTokens")
    fun clearTokens(@Path("id") personId: Int?, @Query("access_token") accessToken: String): Call<ResponseBody>
}
