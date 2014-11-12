package android.chub.io.chub.data.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
    public static final String PRODUCTION_API_URL = "https://maps.googleapis.com/maps/api";

    @Provides @Singleton
    Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint(PRODUCTION_API_URL);
    }

    @Provides @Singleton
    Client provideClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides @Singleton
    RestAdapter provideRestAdapter(Endpoint endpoint, Client client, Gson gson) {
        return new RestAdapter.Builder() //
                .setClient(client) //
                .setEndpoint(endpoint) //
                .setConverter(new GsonConverter(gson))//
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        }

    @Provides @Singleton
    Gson provideGsonConverter() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides @Singleton GeocodingService provideGeocodingService(RestAdapter restAdapter) {
        return restAdapter.create(GeocodingService.class);
    }
}
