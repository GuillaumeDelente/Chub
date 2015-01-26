package io.chub.android.data.api.model;

/**
 * Created by guillaume on 11/18/14.
 */
public class Chub {

    public final Destination destination;
    public final long id;
    public String publicId;
    public String travelMode;

    public Chub(Destination destination, long id, String travelMode) {
        this.destination = destination;
        this.id = id;
        this.travelMode = travelMode;
    }
}
