package io.chub.android.data.api.model;

/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleRoute {

    public final OverviewPolyline overview_polyline;
    public final Bounds bounds;

    public GoogleRoute(OverviewPolyline overview_polyline, Bounds bounds) {
        this.overview_polyline = overview_polyline;
        this.bounds = bounds;
    }

    public class OverviewPolyline {

        public final String points;

        public OverviewPolyline(String points) {
            this.points = points;
        }
    }

    public class Bounds {
        public final LocationCouple southwest;
        public final LocationCouple northeast;

        public Bounds(LocationCouple southwest, LocationCouple northeast) {
            this.southwest = southwest;
            this.northeast = northeast;
        }

        public class LocationCouple {
            public final double lat;
            public final double lng;

            public LocationCouple(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }
        }
    }
}
