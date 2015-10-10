package io.chub.android.data.api;

import java.util.List;
import java.util.Map;

import io.chub.android.data.api.model.AuthToken;
import io.chub.android.data.api.model.Chub;
import io.chub.android.data.api.model.ChubLocation;
import retrofit.http.Body;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by guillaume on 11/9/14.
 */
public interface ChubApi {

    @POST("authTokens/") //
    Observable<AuthToken>
    createToken(@Body Map map);

    @POST("chubs/") //
    Observable<Chub>
    createChub(@Body Map map);

    @PATCH("chubs/{chubId}/") //
    Observable<Chub>
    updateChub(@Path("chubId") long chubId, @Body Map map);

    @POST("chubs/{chubId}/locations/") //
    Observable<ChubLocation>
    postLocation(@Path("chubId") long chubId, @Body ChubLocation location);


    @POST("chubs/{chubId}/locations/") //
    Observable<List<ChubLocation>>
    postLocation(@Path("chubId") long chubId, @Body List<ChubLocation> locations);
}
