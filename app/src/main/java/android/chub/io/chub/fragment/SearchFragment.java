package android.chub.io.chub.fragment;

import android.chub.io.chub.BuildConfig;
import android.chub.io.chub.R;
import android.chub.io.chub.data.api.GeocodingService;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleResponse;
import android.chub.io.chub.widget.SearchEditTextLayout;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by guillaume on 11/11/14.
 */
public class SearchFragment extends BaseFragment {

    private static final String TAG = "SearchFragment";
    private SearchEditTextLayout mSearchEditText;
    @Inject
    GeocodingService mGeocodingService;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(android.R.id.list);
    }

    public void setQueryString(String query, String location) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "setQueryString");
        mGeocodingService.getAddress(query, location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GoogleResponse<GoogleAddress>>() {
                    @Override
                    public void call(GoogleResponse<GoogleAddress> googleAddressGoogleResponse) {
                    }
                });;
    }
}
