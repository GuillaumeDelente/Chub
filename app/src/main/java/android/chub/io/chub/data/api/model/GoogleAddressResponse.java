package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleAddressResponse<T> {

    public static final String OK = "OK";

    public final List<T> predictions;

    public final String status;

    public GoogleAddressResponse(List<T> predictions, String status) {
        this.predictions = predictions;
        this.status = status;
    }
}
