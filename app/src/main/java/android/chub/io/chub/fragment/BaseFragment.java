package android.chub.io.chub.fragment;

import android.chub.io.chub.ChubApp;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by guillaume on 11/9/14.
 */
public class BaseFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChubApp app = ChubApp.get(getActivity());
        app.inject(this);
    }
}
