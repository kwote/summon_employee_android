package employee.summon.asano.rest

import employee.summon.asano.model.AddSummonRequest
import employee.summon.asano.model.SummonRequest
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface SummonRequestService {
    @GET("summonrequests")
    fun getSummonRequest(@Path("id") requestId: Int,
                         @Header("Authorization") accessToken: String): Observable<SummonRequest>

    @POST("summonrequests")
    fun addSummonRequest(@Body request: AddSummonRequest,
                         @Header("Authorization") accessToken: String): Observable<SummonRequest>

    @PUT("summonrequests/{id}/accept")
    fun acceptRequest(@Path("id") requestId: Int): Observable<SummonRequest>

    @PUT("summonrequests/{id}/reject")
    fun rejectRequest(@Path("id") requestId: Int): Observable<SummonRequest>

    @PUT("summonrequests/{id}/cancel")
    fun cancelRequest(@Path("id") requestId: Int,
                      @Header("Authorization") accessToken: String): Observable<SummonRequest>

    @DELETE("summonrequests/{id}")
    fun deleteRequest(@Path("id") requestId: Int,
                      @Header("Authorization") accessToken: String): Observable<ResponseBody>

    @GET("summonrequests")
    fun listRequests(
            @Query(value = "filter", encoded = true) filter: String,
            @Header("Authorization") accessToken: String
    ): Observable<List<SummonRequest>>
}
