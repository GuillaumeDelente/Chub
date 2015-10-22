package io.chub.android.data.api;


import io.chub.android.data.api.model.GoogleAddress;
import io.chub.android.data.api.model.GoogleAddressResponse;
import io.chub.android.data.api.model.GoogleDirectionResponse;
import io.chub.android.data.api.model.GoogleDurationResponse;
import io.chub.android.data.api.model.GooglePlace;
import io.chub.android.data.api.model.GooglePlaceResponse;
import io.chub.android.data.api.model.GoogleRoute;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by guillaume on 11/9/14.
 */
public interface GeocodingService  {

    /*
    @GET("/geocode/json") //
    Observable<GoogleResponse<GoogleAddress>>
    getAddress(@Query("latlng") String latLng);*/

    @GET("place/autocomplete/json?radius=1000") //
    Observable<GoogleAddressResponse<GoogleAddress>>
    getAddress(@Query("input") String input,//
               @Query("location") String location,//
               @Query("key") String apiKey);

    @GET("place/details/json") //
    Observable<GooglePlaceResponse<GooglePlace>>
    getPlaceDetails(@Query("placeid") String placeId,//
               @Query("key") String apiKey);

    @GET("directions/json") //
    Observable<GoogleDirectionResponse<GoogleRoute>>
    getDirections(@Query("origin") String origin,//
                  @Query("destination") String destination,//
                  @Query("mode") String travelMode,//
                  @Query("key") String apiKey);

    @GET("distancematrix/json") //
    Observable<GoogleDurationResponse>
    getDuration(@Query("origin") String origin, //
                @Query("destination") String destination, //
                @Query("mode") String travelMode, //
                @Query("key") String apiKey); //
}
