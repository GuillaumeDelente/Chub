package android.chub.io.chub.data.api;

import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleResponse;
import android.widget.Gallery;

import retrofit.http.GET;
import retrofit.http.Path;
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
    Observable<GoogleResponse<GoogleAddress>>
    getAddress(@Query("input") String input,//
               @Query("location") String location,//
               @Query("key") String apiKey);
}
