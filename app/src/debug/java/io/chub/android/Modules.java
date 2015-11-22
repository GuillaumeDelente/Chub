package io.chub.android;

/**
 * Created by guillaume on 11/9/14.
 */
final class Modules {

        static Object[] list(ChubApp app) {
            return new Object[] {
                    new ChubModule(app),
                    new DebugChubModule(),
            };
        }

        private Modules() {
            // No instances.
        }
    }
