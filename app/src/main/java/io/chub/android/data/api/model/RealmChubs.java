package io.chub.android.data.api.model;

import io.realm.Realm;

/**
 * Created by guillaume on 11/15/15.
 */
public class RealmChubs {

    public static void fromChub(Realm realm, RealmChub realmChub, Chub chub) {
        realmChub.setId(chub.id);
        if (chub.destination != null) {
            RealmDestination realmDestination = new RealmDestination();
            RealmDestinations.fromDestination(realmDestination, chub.destination);
            realmChub.setDestination(realmDestination);
            realmChub.setTransportationMode(chub.travelMode);
        }
    }

}
