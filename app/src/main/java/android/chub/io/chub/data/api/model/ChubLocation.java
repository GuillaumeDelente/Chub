package android.chub.io.chub.data.api.model;

/**
 * Created by guillaume on 11/19/14.
 */
public class ChubLocation {

    public final double latitude;
    public final double longitude;

    public ChubLocation(double latitude, double longitude) {
        this.latitude = Double.valueOf(String.format("%.10f", latitude));
        this.longitude = Double.valueOf(String.format("%.10f", longitude));
    }
}
