package android.chub.io.chub.data;

import android.app.Application;
import android.chub.io.chub.data.api.ApiModule;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
