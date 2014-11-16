package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/9/14.
 */
public class GooglePlaceResponse<T> {

    public final T result;

    public GooglePlaceResponse(T result) {
        this.result = result;
    }
}
