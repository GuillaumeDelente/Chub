package android.chub.io.chub.fragment;

import android.chub.io.chub.BuildConfig;
import android.chub.io.chub.R;
import android.chub.io.chub.adapter.SearchAdapter;
import android.chub.io.chub.data.api.ApiKey;
import android.chub.io.chub.data.api.GeocodingService;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleResponse;
import android.chub.io.chub.widget.SearchEditTextLayout;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by guillaume on 11/11/14.
 */
public class SearchFragment extends BaseFragment {

    private static final String TAG = "SearchFragment";
    @Inject
    GeocodingService mGeocodingService;
    @Inject
    @ApiKey
    String mGoogleApiKey;
    private SearchAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
    }

    public void setQueryString(String query, String location) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "setQueryString");
        if (TextUtils.isEmpty(query))
            return;
        mGeocodingService.getAddress(query, location, mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GoogleResponse<GoogleAddress>>() {
                    @Override
                    public void call(GoogleResponse<GoogleAddress> googleAddressGoogleResponse) {
                        mAdapter.updateItems(googleAddressGoogleResponse.predictions);
                    }
                });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SearchAdapter(new ArrayList<GoogleAddress>(0));
        mRecyclerView.setAdapter(mAdapter);
    }
}
