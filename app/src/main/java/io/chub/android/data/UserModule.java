package io.chub.android.data;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.util.UserPreferences;

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
