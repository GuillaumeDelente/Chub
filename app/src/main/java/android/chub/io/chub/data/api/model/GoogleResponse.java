package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleResponse<T> {

    public final List<T> results;

    public GoogleResponse(List<T> results) {
        this.results = results;
    }
}
