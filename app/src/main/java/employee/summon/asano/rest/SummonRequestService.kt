package employee.summon.asano.rest

import employee.summon.asano.model.SummonRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface SummonRequestService {
    @GET("summonrequests")
    fun listOutgoingRequests(@Query("filter[where][callerId]") callerId: Int?): Call<List<SummonRequest>>

    @GET("summonrequests")
    fun listIncomingRequests(@Query("filter[where][targetId]") targetId: Int?): Call<List<SummonRequest>>

    @GET("summonrequests")
    fun getSummonRequest(@Path("id") requestId: Int?): Call<SummonRequest>

    @POST("summonrequests")
    fun addSummonRequest(@Body request: SummonRequest): Call<SummonRequest>

    @PUT("summonrequests/{id}/accept")
    fun acceptRequest(@Path("id") requestId: Int?): Call<SummonRequest>

    @PUT("summonrequests/{id}/reject")
    fun rejectRequest(@Path("id") requestId: Int?): Call<SummonRequest>

    @PUT("summonrequests/{id}/cancel")
    fun cancelRequest(@Path("id") requestId: Int?): Call<SummonRequest>

    @DELETE("summonrequests/{id}")
    fun deleteRequest(@Path("id") requestId: Int?): Call<ResponseBody>

    @GET("summonrequests/findOne")
    fun getSummonRequest(
            @Query("filter[where][callerId]") callerId: Int,
            @Query("filter[where][targetId]") targetId: Int,
            @Query("filter[where][enabled]") enabled: Boolean): Call<SummonRequest>
}
