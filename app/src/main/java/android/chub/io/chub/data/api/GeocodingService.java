package android.chub.io.chub.data.api;

import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GooglePlace;
import android.chub.io.chub.data.api.model.GooglePlaceResponse;
import android.chub.io.chub.data.api.model.GoogleRoute;
import android.chub.io.chub.data.api.model.GoogleAddressResponse;
import android.chub.io.chub.data.api.model.GoogleDirectionResponse;

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

    @GET("/place/autocomplete/json?types=address&radius=1000") //
    Observable<GoogleAddressResponse<GoogleAddress>>
    getAddress(@Query("input") String input,//
               @Query("location") String location,//
               @Query("key") String apiKey);

    @GET("/place/details/json") //
    Observable<GooglePlaceResponse<GooglePlace>>
    getPlaceDetails(@Query("placeid") String placeId,//
               @Query("key") String apiKey);

    @GET("/directions/json") //
    Observable<GoogleDirectionResponse<GoogleRoute>>
    getDirections(@Query("origin") String origin,//
               @Query("destination") String destination,//
               @Query("key") String apiKey);
}
