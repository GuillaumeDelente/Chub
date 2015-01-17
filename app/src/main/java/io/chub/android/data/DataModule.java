package io.chub.android.data;

import android.app.Application;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.data.api.ApiModule;
import io.realm.Realm;

/**
 * Created by guillaume on 11/9/14.
 */

@Module(
        includes = {
                ApiModule.class,
                UserModule.class,
        },
        complete = false,
        library = true
)
public class DataModule {

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return new OkHttpClient();
    }

    @Provides
    @Singleton
    Realm provideRealm(Application app) {
        return Realm.getInstance(app);
    }

}
