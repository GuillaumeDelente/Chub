package io.chub.android.data.api.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by guillaume on 11/19/14.
 */
public class ChubLocation {

    public final double latitude;
    public final double longitude;
    public final Date createdAt;

    public ChubLocation(double latitude, double longitude) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.createdAt = c.getTime();
        this.latitude = Double.valueOf(String.format("%.10f", latitude));
        this.longitude = Double.valueOf(String.format("%.10f", longitude));
    }
}
