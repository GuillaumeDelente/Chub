package io.chub.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.chub.android.ChubApp;


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
