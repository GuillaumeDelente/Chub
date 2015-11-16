package io.chub.android.data.api.model;

import io.realm.RealmObject;

/**
 * Created by guillaume on 11/25/14.
 */
public class RealmChub extends RealmObject {

    private RealmDestination destination;
    private long id;

    public RealmDestination getDestination() {
        return destination;
    }

    public long getId() {
        return id;
    }

    public void setDestination(RealmDestination destination) {
        this.destination = destination;
    }

    public void setId(long id) {
        this.id = id;
    }
}
