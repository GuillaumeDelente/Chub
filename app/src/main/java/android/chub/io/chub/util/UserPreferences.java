package android.chub.io.chub.util;

import android.content.Context;
import android.content.SharedPreferences;

import info.metadude.android.typedpreferences.StringPreference;

/**
 * Created by guillaume on 11/18/14.
 */
public class UserPreferences {
    private static final String USER_PREFERENCES = "user_preferences";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private final SharedPreferences mUserPreferences;
    private final StringPreference mAuthTokenPreference;

    public UserPreferences(Context context) {
        mUserPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        mAuthTokenPreference = new StringPreference(mUserPreferences, KEY_AUTH_TOKEN);
    }

    public StringPreference getAuthTokenPreference() {
        return mAuthTokenPreference;
    }
}
