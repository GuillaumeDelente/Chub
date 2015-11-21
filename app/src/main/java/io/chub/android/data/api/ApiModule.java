package io.chub.android.data.api;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.chub.android.BuildConfig;
import io.chub.android.R;
import io.chub.android.data.api.model.GmtDateTypeAdapter;
import io.chub.android.data.api.model.Terms;
import io.chub.android.data.api.model.TermsTypeAdapter;
import io.chub.android.util.UserPreferences;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Created by guillaume on 11/9/14.
 */

@Module(
        complete = false,
        library = true
)
public class ApiModule {
    public static final String GOOGLE_PRODUCTION_API_URL = "https://maps.googleapis.com/maps/api/";
    public static final String API_PRODUCTION_API_URL = "https://www.chub.io/api/";

    @Provides @Singleton @GoogleRestAdapter
    String provideGoogleEndpoint() {
        return GOOGLE_PRODUCTION_API_URL;
    }

    @Provides @Singleton @ChubRestAdapter
    String provideApiEndpoint() {
        return API_PRODUCTION_API_URL;
    }

    @Provides @Singleton @ChubRestAdapter
    Retrofit provideChubRestAdapter(@ChubRestAdapter String endpoint,
                                    @ChubRestAdapter OkHttpClient client,
                                    @ChubRestAdapter Gson gson) {
        return new Retrofit.Builder() //
                .client(client) //
                .baseUrl(endpoint) //
                .addConverterFactory(GsonConverterFactory.create(gson)) //
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //
                .build();
    }

    @Provides @Singleton @GoogleRestAdapter
    Retrofit provideGoogleRestAdapter(@GoogleRestAdapter String endpoint,
                                      @GoogleRestAdapter OkHttpClient client,
                                      @GoogleRestAdapter Gson gson) {
        return new Retrofit.Builder() //
                .client(client) //
                .baseUrl(endpoint) //
                .addConverterFactory(GsonConverterFactory.create(gson)) //
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //
                .build();
    }

    @Provides @Singleton @GoogleRestAdapter
    Gson provideGoogleGsonConverter() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Terms.class, new TermsTypeAdapter())
                .create();
    }

    @Provides @Singleton @ChubRestAdapter
    Gson provideChubGsonConverter() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Terms.class, new TermsTypeAdapter())
                .registerTypeAdapter(Date.class, new GmtDateTypeAdapter())
                .create();
    }

    @Provides @Singleton GeocodingService provideGeocodingService(@GoogleRestAdapter Retrofit restAdapter) {
        return restAdapter.create(GeocodingService.class);
    }

    @Provides @Singleton
    ChubApi provideChubServiceService(@ChubRestAdapter Retrofit restAdapter) {
        return restAdapter.create(ChubApi.class);
    }

    @Provides @Singleton @ApiKey String provideApiKey(Context context) {
        return context.getString(R.string.place_api_key);
    }

    @Provides @Singleton @ChubRestAdapter
    OkHttpClient provideChubOkHttpClient(final UserPreferences userPreferences) {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                if (userPreferences.getAuthTokenPreference().isSet()) {
                    Request.Builder builder = originalRequest.newBuilder()
                            .addHeader("XChubAuthToken",
                                    userPreferences.getAuthTokenPreference().get());
                    originalRequest = builder.build();
                }
                return chain.proceed(originalRequest);
            }
        });
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);
        client.networkInterceptors().add(logging);
        return client;
    }

    @Provides @Singleton @GoogleRestAdapter
    OkHttpClient provideGoogleOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);
        client.networkInterceptors().add(logging);
        return client;
    }
}
