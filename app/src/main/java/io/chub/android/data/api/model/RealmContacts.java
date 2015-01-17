package io.chub.android.data.api.model;

import io.realm.RealmObject;

/**
 * Created by guillaume on 11/25/14.
 */
public class RealmContacts extends RealmObject {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
