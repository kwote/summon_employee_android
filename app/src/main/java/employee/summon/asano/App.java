package employee.summon.asano;

import android.app.Application;

import employee.summon.asano.model.AccessToken;

public class App extends Application {
    private AccessToken accessToken = null;

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }
}
