package employee.summon.asano.rest

import employee.summon.asano.model.*
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface PeopleService {
    @PUT("people/ping")
    fun ping(@Header("Authorization") accessToken: String): Observable<Boolean>

    @GET("people")
    fun listPeople(@Header("Authorization") accessToken: String): Observable<List<Person>>

    @GET("people/{id}")
    fun getPerson(@Path("id") personId: Int, @Header("Authorization") accessToken: String): Observable<Person>

    @POST("people")
    fun addPerson(@Body person: AddPerson): Observable<Person>

    @POST("people/login")
    fun login(@Body credentials: LoginCredentials, @Query("include") obj: String = "user"): Observable<AccessToken>

    @POST("people/logout")
    fun logout(@Header("Authorization") accessToken: String): Observable<ResponseBody>

    @GET("people/{id}/incomingRequests")
    fun listIncomingRequests(
            @Path("id") personId: Int?, @Header("Authorization") accessToken: String,
            @Query("filter[include]") obj: String = "caller"
    ): Observable<List<SummonRequest>>

    @GET("people/{id}/outgoingRequests")
    fun listOutgoingRequests(
            @Path("id") personId: Int?, @Header("Authorization") accessToken: String,
            @Query("filter[include]") obj: String = "target"
    ): Observable<List<SummonRequest>>
}
