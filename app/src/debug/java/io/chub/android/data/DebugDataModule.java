package io.chub.android.data;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.R;

/**
 * Created by guillaume on 11/22/15.
 */
@Module(
        complete = false,
        library = true,
        overrides = true
)
public final class DebugDataModule {

    @Provides
    @Singleton
    Tracker provideGoogleAnalyticsTracker(Application app) {
        return GoogleAnalytics.getInstance(app).newTracker(R.xml.dev_tracker);
    }

}
