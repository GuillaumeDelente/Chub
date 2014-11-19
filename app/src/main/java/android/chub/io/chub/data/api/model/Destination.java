package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/18/14.
 */
public class Destination {

        public final String name;
        public final Double latitude;
        public final Double longitude;

        public Destination(String name, Double latitude, Double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
}
