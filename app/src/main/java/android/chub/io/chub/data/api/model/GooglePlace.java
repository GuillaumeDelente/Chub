package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/9/14.
 */
public class GooglePlace {

    public final Geometry geometry;
    public final String id;
    public final String name;

    public GooglePlace(Geometry geometry, String id, String name) {
        this.geometry = geometry;
        this.id = id;
        this.name = name;
    }

    public class Geometry {

        public final Location location;

        Geometry(Location location) {
            this.location = location;
        }

        public class Location {
            public final Float lat;
            public final Float lng;

            public Location(Float lat, Float lng) {
                this.lat = lat;
                this.lng = lng;
            }
        }
    }

}
