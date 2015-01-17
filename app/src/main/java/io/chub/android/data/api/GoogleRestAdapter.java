package io.chub.android.data.api;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by guillaume on 11/12/14.
 */
@Qualifier
@Retention(RUNTIME)
public @interface GoogleRestAdapter {
}