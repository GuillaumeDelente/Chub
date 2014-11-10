package io.chub.android.chub;

import android.chub.io.chub.ChubApp;

final class Modules {
    static Object[] list(ChubApp app) {
        return new Object[] {
                new ChubModule(app)
        };
    }

    private Modules() {
        // No instances.
    }
}