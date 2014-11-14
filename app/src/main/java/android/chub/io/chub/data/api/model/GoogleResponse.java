package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleResponse<T> {

    public final List<T> predictions;

    public GoogleResponse(List<T> predictions) {
        this.predictions = predictions;
    }
}
