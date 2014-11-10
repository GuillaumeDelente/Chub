package android.chub.io.chub;

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
