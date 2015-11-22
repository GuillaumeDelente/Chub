package io.chub.android.data.api.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by guillaume on 11/25/14.
 */
public class RealmRecentChub extends RealmObject {

    private RealmList<RealmContact> contacts;
    private RealmDestination destination;
    private long lastUsed;

    public RealmList<RealmContact> getContacts() {
        return this.contacts;
    }

    public void setContacts(RealmList<RealmContact> contacts) {
        this.contacts = contacts;
    }

    public RealmDestination getDestination() {
        return destination;
    }

    public void setDestination(RealmDestination destination) {
        this.destination = destination;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

}
