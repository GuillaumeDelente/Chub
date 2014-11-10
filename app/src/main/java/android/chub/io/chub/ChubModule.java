package android.chub.io.chub;

import android.app.Application;
import android.chub.io.chub.activity.ChubActivity;
import android.chub.io.chub.data.DataModule;
import android.chub.io.chub.fragment.MapFragment;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

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
                MapFragment.class,
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
    }
