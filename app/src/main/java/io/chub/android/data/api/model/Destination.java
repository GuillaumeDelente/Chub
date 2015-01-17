package io.chub.android.data.api.model;

/**
 * Created by guillaume on 11/18/14.
 */
public class Destination {

    public final String id;
    public final String name;
    public final Double latitude;
    public final Double longitude;

    public Destination(String id, String name, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
