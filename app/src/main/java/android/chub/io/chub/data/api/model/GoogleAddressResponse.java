package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleAddressResponse<T> {

    public final List<T> predictions;

    public GoogleAddressResponse(List<T> predictions) {
        this.predictions = predictions;
    }
}
