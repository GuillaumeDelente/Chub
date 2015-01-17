package io.chub.android;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.activity.ChubActivity;
import io.chub.android.activity.ShareActivity;
import io.chub.android.data.DataModule;
import io.chub.android.fragment.MapFragment;
import io.chub.android.fragment.SearchFragment;
import io.chub.android.service.ChubLocationService;

/**
 * Created by guillaume on 11/9/14.
 */
@Module(
        includes = {
                DataModule.class
        },
        injects = {
                ChubApp.class,
                ChubActivity.class,
                ShareActivity.class,
                MapFragment.class,
                SearchFragment.class,
                ChubLocationService.class,
                ChubChooserActivity.class,
        }
)

public class ChubModule {
    private final ChubApp app;

    public ChubModule(ChubApp app) {
            this.app = app;
        }

    @Provides @Singleton Application provideApplication() {
            return app;
        }

    @Provides @Singleton
    Context provideApplicationContext() {
        return app.getApplicationContext();
    }
}
