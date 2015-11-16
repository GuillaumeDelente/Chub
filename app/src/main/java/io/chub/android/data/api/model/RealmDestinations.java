package io.chub.android.data.api.model;

/**
 * Created by guillaume on 11/15/15.
 */
public class RealmDestinations {

    public static void fromDestination(RealmDestination realmDestination, Destination destination) {
        realmDestination.setPlaceId(destination.id);
        realmDestination.setName(destination.name);
        realmDestination.setLatitude(destination.latitude);
        realmDestination.setLongitude(destination.longitude);
    }
}
