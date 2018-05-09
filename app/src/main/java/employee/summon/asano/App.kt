package employee.summon.asano

import android.app.Application
import android.content.Intent
import android.os.Build

import employee.summon.asano.model.AccessToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    var accessToken: AccessToken? = null

    var retrofit: Retrofit? = null
        private set

    override fun onCreate() {
        super.onCreate()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }

    val services : MutableMap<String, Any> = HashMap()

    inline fun <reified T> getService(): T {
        if (services.contains(T::class.java.simpleName)) {
            return services[T::class.java.simpleName] as T
        }
        val service = retrofit!!.create<T>(T::class.java)
        services[T::class.java.simpleName] = service as Any
        return service
    }

    companion object {
        const val REQUEST_URL_SUFFIX = "summonrequests/change-stream/"
        const val REQUEST_RECEIVED = "employee.summon.asano.REQUEST_RECEIVED"
        const val REQUEST = "request_extra"
        const val ACCESS_TOKEN = "access_token"
    }
}
