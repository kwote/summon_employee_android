package employee.summon.asano.rest

import employee.summon.asano.model.SummonRequest
import retrofit2.Call
import retrofit2.http.*

interface SummonRequestService {
    @GET("summonrequests")
    fun listRequests(@Query("callerId") callerId: Int?): Call<List<SummonRequest>>

    @GET("summonrequests")
    fun getSummonRequest(@Path("id") requestId: Int?): Call<SummonRequest>

    @POST("summonrequests")
    fun addSummonRequest(@Body request: SummonRequest): Call<SummonRequest>
}
