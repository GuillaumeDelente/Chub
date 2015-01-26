package io.chub.android;

import android.app.Application;
import android.content.Context;

import com.bugsnag.android.Bugsnag;

import dagger.ObjectGraph;
import timber.log.Timber;

/**
 * Created by guillaume on 11/9/14.
 */
public class ChubApp extends Application {

        private ObjectGraph objectGraph;

        @Override public void onCreate() {
            super.onCreate();
            Bugsnag.init(this);
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
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
