package io.chub.android;

import io.chub.android.ChubApp;
import io.chub.android.ChubModule;

/**
 * Created by guillaume on 11/9/14.
 */
final class Modules {

        static Object[] list(ChubApp app) {
            return new Object[] {
                    new ChubModule(app),
            };
        }

        private Modules() {
            // No instances.
        }
    }
