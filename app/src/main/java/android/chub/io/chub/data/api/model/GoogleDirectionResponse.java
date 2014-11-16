package android.chub.io.chub.data.api.model;

import java.util.List;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleDirectionResponse<T> {

    public final List<T> routes;

    public GoogleDirectionResponse(List<T> routes) {
        this.routes = routes;
    }
}
