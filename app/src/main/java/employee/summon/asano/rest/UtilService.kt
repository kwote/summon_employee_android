package employee.summon.asano.rest

import io.reactivex.Observable
import retrofit2.http.*

interface UtilService {
    @PUT("people/ping")
    fun ping(@Header("Authorization") accessToken: String): Observable<Boolean>
}
