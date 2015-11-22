package io.chub.android;

import dagger.Module;
import io.chub.android.data.DebugDataModule;

/**
 * Created by guillaume on 11/22/15.
 */
@Module(
        addsTo = ChubModule.class,
        includes = {
                DebugDataModule.class
        },
        overrides = true
)
public final class DebugChubModule {

}
