package employee.summon.asano;

import android.app.Application;

import employee.summon.asano.model.AccessToken;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {
    private AccessToken accessToken = null;

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private Retrofit retrofit;

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }
}
