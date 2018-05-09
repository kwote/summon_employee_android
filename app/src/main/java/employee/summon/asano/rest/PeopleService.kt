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

    @GET("people/{id}")
    fun getPerson(@Path("id") personId: Int?): Call<Person>

    @POST("people")
    fun addPerson(@Body person: AddPerson): Call<Person>

    @POST("people/login")
    fun login(@Body credentials: LoginCredentials, @Query("include") obj: String = "user"): Call<AccessToken>

    @POST("people/logout")
    fun logout(@Header("Authorization") accessToken: String): Call<ResponseBody>

    @DELETE("people/{id}/accessTokens")
    fun clearTokens(@Path("id") personId: Int?, @Header("Authorization") accessToken: String): Call<ResponseBody>
}
