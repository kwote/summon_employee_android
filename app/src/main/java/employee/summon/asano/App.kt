package employee.summon.asano

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.util.Log
import employee.summon.asano.model.Person
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {
    lateinit var accessToken: String
    var serverUrl: String = ""
        get () {
            if (TextUtils.isEmpty(field)) {
                return getString(R.string.base_url)
            }
            return field
        }
        set (value) {
            field = value
            retrofit = retrofitClient(value)
            services.clear()
        }

    fun serverAvailable() = retrofit != null

    private fun retrofitClient(serverUrl: String): Retrofit? {
        try {
            return Retrofit.Builder()
                    .baseUrl("$serverUrl/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                    .build()
        } catch (e: Exception) {
            Log.e("Retrofit", "Failed to init $serverUrl", e)
        }
        return null
    }

    lateinit var user: Person

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    var retrofit: Retrofit? = null
        get() {
            if (field == null) {
                field = retrofitClient(serverUrl)
                services.clear()
            }
            return field
        }

    val services : MutableMap<String, Any> = HashMap()

    inline fun <reified T> getService(): T {
        if (services.contains(T::class.java.simpleName)) {
            return services[T::class.java.simpleName] as T
        }
        val service = retrofit?.create<T>(T::class.java)
        services[T::class.java.simpleName] = service as Any
        return service
    }

    companion object {
        fun getApp(context: Context) = context.applicationContext as App

        const val REQUEST = "request_extra"
        const val ACCESS_TOKEN = "access_token"
        const val SERVER_URL = "server_url"
    }
}
