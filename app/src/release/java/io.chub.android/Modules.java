package io.chub.android;

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