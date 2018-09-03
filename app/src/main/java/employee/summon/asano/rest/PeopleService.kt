package employee.summon.asano.rest

import employee.summon.asano.model.*
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface PeopleService {
    @PUT("people/ping")
    fun ping(@Query("token") accessToken: String): Observable<Boolean>

    @GET("people/canSummon")
    fun canSummon(@Query("targetId") targetId: Int, @Header("Authorization") accessToken: String): Observable<Boolean>

    @GET("people")
    fun listPeople(@Header("Authorization") accessToken: String): Observable<List<Person>>

    @GET("people/summon")
    fun listSummonPeople(@Header("Authorization") accessToken: String): Observable<List<SummonPerson>>

    @GET("people/{id}")
    fun getPerson(@Path("id") personId: Int, @Header("Authorization") accessToken: String): Observable<Person>

    @POST("people")
    fun addPerson(@Body person: AddPerson): Observable<Person>

    @POST("people/login")
    fun login(@Body credentials: LoginCredentials, @Query("include") user: String = "user"): Observable<AccessToken>

    @POST("people/logout")
    fun logout(@Header("Authorization") accessToken: String): Observable<ResponseBody>

    @GET("people/{id}/incomingRequests")
    fun listIncomingRequests(
            @Path("id") targetId: Int, @Header("Authorization") accessToken: String,
            @Query(value = "filter", encoded = true) filter: String
    ): Observable<List<SummonRequest>>

    @GET("people/{id}/outgoingRequests")
    fun listOutgoingRequests(
            @Path("id") callerId: Int, @Header("Authorization") accessToken: String,
            @Query(value = "filter", encoded = true) filter: String
    ): Observable<List<SummonRequest>>
}
