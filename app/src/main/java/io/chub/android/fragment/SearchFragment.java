package io.chub.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import io.chub.android.BuildConfig;
import io.chub.android.R;
import io.chub.android.activity.ChubActivity;
import io.chub.android.adapter.RecentAdapter;
import io.chub.android.adapter.SearchAdapter;
import io.chub.android.data.api.ApiKey;
import io.chub.android.data.api.ErrorHandler;
import io.chub.android.data.api.GeocodingService;
import io.chub.android.data.api.model.GoogleAddress;
import io.chub.android.data.api.model.GoogleAddressResponse;
import io.chub.android.data.api.model.RealmRecentChub;
import io.chub.android.widget.DividerItemDecoration;
import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by guillaume on 11/11/14.
 */
public class SearchFragment extends BaseFragment {

    private static final String TAG = "SearchFragment";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_QUERY = "query";
    @Inject
    GeocodingService mGeocodingService;
    @Inject
    @ApiKey
    String mGoogleApiKey;
    Realm mRealm;
    private SearchAdapter mPlacesAdapter;
    private RecentAdapter mRecentAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mCurrentQuery = "";
    private String mLocation = null;

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

    public void setQueryString(final String query, final String location) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "setQueryString");
        if (TextUtils.isEmpty(query)) {
            mCurrentQuery = "";
            mPlacesAdapter.updateItems(new ArrayList<GoogleAddress>(0));
            mRecyclerView.setAdapter(mRecentAdapter);
            return;
        }
        mLocation = location;
        mCurrentQuery = query;
        mGeocodingService.getAddress(query, location, mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<GoogleAddressResponse<GoogleAddress>, Boolean>() {
                    @Override
                    public Boolean call(GoogleAddressResponse<GoogleAddress> response) {
                        return mCurrentQuery.equals(query);
                    }
                })
                .subscribe(new Subscriber<GoogleAddressResponse<GoogleAddress>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ErrorHandler.showError(getActivity(), e);
                    }

                    @Override
                    public void onNext(GoogleAddressResponse<GoogleAddress> result) {
                        if (!GoogleAddressResponse.OK.equals(result.status)) {
                            Toast.makeText(getActivity(), getString(R.string.http_error),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mPlacesAdapter.updateItems(result.predictions);
                        }
                    }
                });
        mRecyclerView.setAdapter(mPlacesAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();
        mRealm = Realm.getInstance(activity);
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL_LIST,
                activity.getResources().getDimensionPixelSize(R.dimen.divider_padding_left)));

        // specify an adapter (see also next example)
        mPlacesAdapter = new SearchAdapter(new ArrayList<GoogleAddress>(0), activity);
        RealmQuery<RealmRecentChub> query = mRealm.where(RealmRecentChub.class);
        mRecentAdapter = new RecentAdapter(query.findAllSorted("lastUsed", false));
        mRecentAdapter.setOnRecentChubClickListener(
                new RecentAdapter.OnRecentChubClickListener() {
                    @Override
                    public void onRecentChubClicked(final RealmRecentChub recentChub) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Recent chub clicked");
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.send_chub)
                                .setMessage(R.string.send_chub_confirm)
                                .setNegativeButton(R.string.nope, null)
                                .setPositiveButton(getResources()
                                                .getQuantityString(R.plurals.send_to_,
                                                        recentChub.getContacts().size(),
                                                        recentChub.getContacts().size()),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((ChubActivity) activity).onRecentChubSelected(recentChub);
                                    }
                                })
                                .show();
                    }
                });
        mPlacesAdapter.setOnLocationClickListener(new SearchAdapter.LocationClickListener() {
            @Override
            public void onLocationClicked(GoogleAddress address) {
                ((ChubActivity) activity).onDestinationSelected(address);
            }
        });
        mRecyclerView.setAdapter(mRecentAdapter);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(KEY_LOCATION);
            mCurrentQuery = savedInstanceState.getString(KEY_QUERY);
        }
        if (mLocation != null)
            setQueryString(mCurrentQuery, mLocation);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_LOCATION, mLocation);
        outState.putString(KEY_QUERY, mCurrentQuery);
    }

}
