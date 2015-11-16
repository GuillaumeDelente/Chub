package io.chub.android.data;

import dagger.Module;
import io.chub.android.data.api.ApiModule;

/**
 * Created by guillaume on 11/9/14.
 */

@Module(
        includes = {
                ApiModule.class,
                UserModule.class,
        },
        complete = false,
        library = true
)
public class DataModule {

}
