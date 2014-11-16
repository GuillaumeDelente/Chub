package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleRoute {

    public final OverviewPolyline overview_polyline;

    public GoogleRoute(OverviewPolyline overview_polyline) {
        this.overview_polyline = overview_polyline;
    }

    public class OverviewPolyline {

        public final String points;

        public OverviewPolyline(String points) {
            this.points = points;
        }
    }
}
