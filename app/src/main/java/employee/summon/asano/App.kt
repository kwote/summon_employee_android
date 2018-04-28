package employee.summon.asano

import android.app.Application

import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.Person
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
}
