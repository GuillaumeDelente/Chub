package android.chub.io.chub.activity;

import android.chub.io.chub.ChubApp;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by guillaume on 11/9/14.
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChubApp app = ChubApp.get(this);
        app.inject(this);
    }
}
