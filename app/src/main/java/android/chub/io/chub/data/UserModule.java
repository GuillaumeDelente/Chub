package android.chub.io.chub.data;

import android.app.Application;
import android.chub.io.chub.data.api.ApiModule;
import android.chub.io.chub.util.UserPreferences;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by guillaume on 11/9/14.
 */

@Module(
        complete = false,
        library = true
)
public class UserModule {

    @Provides
    @Singleton
    UserPreferences provideUserPreference(Application app) {
        return new UserPreferences(app);
    }

}
