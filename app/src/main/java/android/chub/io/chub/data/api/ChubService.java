package android.chub.io.chub.data.api;

import android.chub.io.chub.data.api.model.AuthToken;
import android.chub.io.chub.data.api.model.Chub;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleAddressResponse;
import android.chub.io.chub.data.api.model.GoogleDirectionResponse;
import android.chub.io.chub.data.api.model.GooglePlace;
import android.chub.io.chub.data.api.model.GooglePlaceResponse;
import android.chub.io.chub.data.api.model.GoogleRoute;

import java.util.Map;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by guillaume on 11/9/14.
 */
public interface ChubService {

    @POST("/authTokens/") //
    Observable<AuthToken>
    createToken(@Body Map map);

    @POST("/chubs/") //
    Observable<Chub>
    createChub(@Body Map map);
}
