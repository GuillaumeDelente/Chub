package android.chub.io.chub.data.api;

import android.app.Application;
import android.chub.io.chub.R;
import android.chub.io.chub.data.api.model.GmtDateTypeAdapter;
import android.chub.io.chub.data.api.model.Terms;
import android.chub.io.chub.data.api.model.TermsTypeAdapter;
import android.chub.io.chub.util.UserPreferences;
import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by guillaume on 11/9/14.
 */

@Module(
        complete = false,
        library = true
)
public class ApiModule {
    public static final String GOOGLE_PRODUCTION_API_URL = "https://maps.googleapis.com/maps/api";
    public static final String API_PRODUCTION_API_URL = "http://api.chub.io";

    @Provides @Singleton @GoogleRestAdapter
    Endpoint provideGoogleEndpoint() {
        return Endpoints.newFixedEndpoint(GOOGLE_PRODUCTION_API_URL);
    }

    @Provides @Singleton @ChubRestAdapter
    Endpoint provideApiEndpoint() {
        return Endpoints.newFixedEndpoint(API_PRODUCTION_API_URL);
    }

    @Provides @Singleton
    Client provideClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides @Singleton @ChubRestAdapter
    RequestInterceptor provideRequestInterceptor(Application app, UserPreferences userPreferences) {
        return new ApiHeadersRequestInterceptor(app, userPreferences);
    }

    @Provides @Singleton @ChubRestAdapter
    RestAdapter provideChubRestAdapter(@ChubRestAdapter Endpoint endpoint, Client client,
                                         @ChubRestAdapter RequestInterceptor requestInterceptor,
                                         @ChubRestAdapter Gson gson) {
        return new RestAdapter.Builder() //
                .setClient(client) //
                .setEndpoint(endpoint) //
                .setConverter(new GsonConverter(gson))//
                .setLogLevel(RestAdapter.LogLevel.FULL)//
                .setRequestInterceptor(requestInterceptor)
                .build();
    }

    @Provides @Singleton @GoogleRestAdapter
    RestAdapter provideGoogleRestAdapter(@GoogleRestAdapter Endpoint endpoint, Client client,
                                         @GoogleRestAdapter Gson gson) {
        return new RestAdapter.Builder() //
                .setClient(client) //
                .setEndpoint(endpoint) //
                .setConverter(new GsonConverter(gson))//
                .setLogLevel(RestAdapter.LogLevel.FULL)//
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

    @Provides @Singleton GeocodingService provideGeocodingService(@GoogleRestAdapter RestAdapter restAdapter) {
        return restAdapter.create(GeocodingService.class);
    }

    @Provides @Singleton
    ChubApi provideChubServiceService(@ChubRestAdapter RestAdapter restAdapter) {
        return restAdapter.create(ChubApi.class);
    }

    @Provides @Singleton @ApiKey String provideApiKey(Context context) {
        return context.getString(R.string.place_api_key);
    }
}
