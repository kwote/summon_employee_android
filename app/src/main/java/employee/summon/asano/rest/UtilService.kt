package employee.summon.asano.rest

import retrofit2.Call
import retrofit2.http.*

interface UtilService {
    @PUT("people/ping")
    fun ping(@Header("Authorization") accessToken: String): Call<Boolean>
}
