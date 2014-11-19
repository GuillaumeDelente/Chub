package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/18/14.
 */
public class Chub {

    public final Destination destination;
    public final long id;

    public Chub(Destination destination, long id) {
        this.destination = destination;
        this.id = id;
    }

    public class Destination {
        public final String name;
        public final Float latitude;
        public final Float longitude;

        public Destination(String name, Float latitude, Float longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
