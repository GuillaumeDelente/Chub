package android.chub.io.chub.data.api;

import android.app.Application;
import android.chub.io.chub.util.UserPreferences;

import retrofit.RequestInterceptor;
import retrofit.client.Client;

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
