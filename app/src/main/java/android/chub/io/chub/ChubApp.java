package android.chub.io.chub;

import android.app.Application;
import android.content.Context;
import dagger.ObjectGraph;
import timber.log.Timber;

import javax.inject.Inject;

/**
 * Created by guillaume on 11/9/14.
 */
public class ChubApp extends Application {

        private ObjectGraph objectGraph;

        @Override public void onCreate() {
            super.onCreate();

            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
            } else {
                // TODO Crashlytics.start(this);
                // TODO Timber.plant(new CrashlyticsTree());
            }

            buildObjectGraphAndInject();
        }

        public void buildObjectGraphAndInject() {
            objectGraph = ObjectGraph.create(Modules.list(this));
            objectGraph.inject(this);
        }

        public void inject(Object o) {
            objectGraph.inject(o);
        }

        public static ChubApp get(Context context) {
            return (ChubApp) context.getApplicationContext();
        }
}
