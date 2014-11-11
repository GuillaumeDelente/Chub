package android.chub.io.chub.activity;

import android.chub.io.chub.R;
import android.chub.io.chub.fragment.MapFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public class ChubActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, Fragment.instantiate(this, MapFragment.class.getName(), null))
                    .commit();
        }
    }
}
