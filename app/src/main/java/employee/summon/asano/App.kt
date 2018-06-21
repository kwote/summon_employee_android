package employee.summon.asano

import android.app.Application
import android.content.Context
import employee.summon.asano.model.Person
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {
    lateinit var accessToken: String
    lateinit var user: Person

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
    }

    val services : MutableMap<String, Any> = HashMap()

    inline fun <reified T> getService(): T {
        if (services.contains(T::class.java.simpleName)) {
            return services[T::class.java.simpleName] as T
        }
        val service = retrofit.create<T>(T::class.java)
        services[T::class.java.simpleName] = service as Any
        return service
    }

    companion object {
        fun getApp(context: Context) = context.applicationContext as App

        const val REQUEST = "request_extra"
        const val ACCESS_TOKEN = "access_token"
    }
}
