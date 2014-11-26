package android.chub.io.chub.data.api.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by guillaume on 11/25/14.
 */
public class RealmLastChub extends RealmObject {

    private RealmList<RealmContacts> contacts;
    private RealmDestination destination;

    public RealmList<RealmContacts> getContacts() {
        return this.contacts;
    }

    public void setContacts(RealmList<RealmContacts> contacts) {
        this.contacts = contacts;
    }

    public RealmDestination getDestination() {
        return destination;
    }

    public void setDestination(RealmDestination destination) {
        this.destination = destination;
    }
}
