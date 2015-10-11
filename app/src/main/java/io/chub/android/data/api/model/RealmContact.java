package io.chub.android.data.api.model;

import io.realm.RealmObject;

/**
 * Created by guillaume on 11/25/14.
 */
public class RealmContact extends RealmObject {

    private String name;
    private String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
