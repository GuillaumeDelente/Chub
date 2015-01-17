package io.chub.android.data.api;

import android.app.Application;

import io.chub.android.util.UserPreferences;
import retrofit.RequestInterceptor;

/**
 * Created by guillaume on 11/18/14.
 */
public class ApiHeadersRequestInterceptor implements RequestInterceptor {

    private final UserPreferences mUserPreferences;

    public ApiHeadersRequestInterceptor(Application app, UserPreferences userPreferences) {
        mUserPreferences = userPreferences;
    }

    @Override
    public void intercept(RequestFacade request) {
        if (mUserPreferences.getAuthTokenPreference().isSet())
            request.addHeader("XChubAuthToken",
                    String.valueOf(mUserPreferences.getAuthTokenPreference().get()));
    }
}
