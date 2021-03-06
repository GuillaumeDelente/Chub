package io.chub.android.data;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.R;
import io.chub.android.data.api.ApiModule;

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
    Tracker provideGoogleAnalyticsTracker(Application app) {
        return GoogleAnalytics.getInstance(app).newTracker(R.xml.prod_tracker);
    }

}
