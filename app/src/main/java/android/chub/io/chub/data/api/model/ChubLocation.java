package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/19/14.
 */
public class ChubLocation {

    public final long chub_id;
    public final double latitude;
    public final double longitude;

    public ChubLocation(long chubId, double latitude, double longitude) {
        this.chub_id = chubId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
