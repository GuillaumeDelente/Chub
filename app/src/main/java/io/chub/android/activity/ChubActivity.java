package io.chub.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.guillaumedelente.android.contacts.activities.ContactSelectionActivity;
import com.melnykov.fab.FloatingActionButton;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import io.chub.android.BuildConfig;
import io.chub.android.R;
import io.chub.android.data.api.ApiKey;
import io.chub.android.data.api.ChubApi;
import io.chub.android.data.api.ErrorAction;
import io.chub.android.data.api.GeocodingService;
import io.chub.android.data.api.model.AuthToken;
import io.chub.android.data.api.model.Chub;
import io.chub.android.data.api.model.Destination;
import io.chub.android.data.api.model.GoogleAddress;
import io.chub.android.data.api.model.GoogleDirectionResponse;
import io.chub.android.data.api.model.GooglePlace;
import io.chub.android.data.api.model.GooglePlaceResponse;
import io.chub.android.data.api.model.GoogleRoute;
import io.chub.android.data.api.model.RealmDestination;
import io.chub.android.data.api.model.RealmRecentChub;
import io.chub.android.fragment.MapFragment;
import io.chub.android.fragment.SearchFragment;
import io.chub.android.service.ChubLocationService;
import io.chub.android.util.DialerUtils;
import io.chub.android.util.UserPreferences;
import io.chub.android.widget.ActionBarController;
import io.chub.android.widget.SearchEditTextLayout;
import io.realm.Realm;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class ChubActivity extends BaseActivity implements ActionBarController.ActivityUi {

    private static final String TAG = "ChubActivity";
    public static final String SEARCH_FRAGMENT = "search_fragment";
    public static final String MAP_FRAGMENT = "map_fragment";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_IN_SEARCH_UI = "in_search_ui";
    private static final int PICK_CONTACTS = 1010;
    public static final String LOCATION_TRACKING_BROADCAST = "location_tracking_broadcast";
    public static final String TRACKING_LOCATION = "location_tracking";
    private ActionBarController mActionBarController;
    private EditText mSearchView;
    private SearchEditTextLayout mSearchEditTextLayout;
    private int mActionBarHeight;
    private String mSearchQuery;
    private boolean mInSearchUi;
    private FrameLayout mParentLayout;
    private Toolbar mToolbar;
    private SearchFragment mSearchFragment;
    private MapFragment mMapFragment;
    private FloatingActionButton mShareLocationFab;
    private Destination mDestination;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(TRACKING_LOCATION)) {
                setupUi(intent.getBooleanExtra(TRACKING_LOCATION, false));
            }
        }
    };
    @InjectViews({ R.id.car_button, R.id.transit_button, R.id.bike_button, R.id.walk_button })
    List<ImageButton> mTransportationModes;

    final ButterKnife.Setter<ImageButton, ImageButton> SELECTED =
            new ButterKnife.Setter<ImageButton, ImageButton>() {
                @Override public void set(ImageButton view, ImageButton selectedMode, int index) {
                    if (view.equals(selectedMode)) {
                        view.setSelected(true);
                        final HashMap body = new HashMap();
                        String travelMode = null;
                        switch (selectedMode.getId()) {
                            case (R.id.transit_button) :
                                travelMode = "transit";
                                break;
                            case (R.id.bike_button) :
                                travelMode = "bicycling";
                                break;
                            case (R.id.walk_button) :
                                travelMode = "walking";
                                break;
                            default :
                            case (R.id.car_button) :
                                travelMode = "driving";
                                break;
                        }
                        body.put("travel_mode", travelMode);
                        mChubApi.updateChub(ChubLocationService.getCurrentlyTrackingChubId(), body)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .onErrorResumeNext(refreshTokenAndRetry(mChubApi.createChub(body)))
                                .subscribe(
                                        new Subscriber<Chub>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                new ErrorAction(ChubActivity.this).call(e);
                                            }

                                            @Override
                                            public void onNext(Chub chub) {
                                                //update ETA
                                            }
                                        });
                    } else {
                        view.setSelected(false);
                    }
                    view.setSelected(view.equals(selectedMode));
                    view.setActivated(view.equals(selectedMode));
                }
            };

    @InjectView(R.id.bottom_layout) View mBottomLayout;
    @Inject
    GeocodingService mGeocodingService;
    @Inject
    @ApiKey
    String mGoogleApiKey;
    @Inject
    ChubApi mChubApi;
    @Inject
    UserPreferences mUserPreferences;
    @Inject
    Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        ButterKnife.inject(this);
        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        final Resources resources = getResources();
        setSupportActionBar(mToolbar);
        mParentLayout = (FrameLayout) findViewById(R.id.container);
        mActionBarHeight = resources.getDimensionPixelSize(R.dimen.action_bar_height_large);
        mSearchEditTextLayout = (SearchEditTextLayout)
                findViewById(R.id.search_view_container);
        mActionBarController = new ActionBarController(this, mSearchEditTextLayout);
        mSearchEditTextLayout.setPreImeKeyListener(mSearchEditTextLayoutListener);
        mSearchView = (EditText) mSearchEditTextLayout.findViewById(R.id.search_view);
        mSearchView.addTextChangedListener(mSearchQueryTextListener);
        mSearchEditTextLayout.findViewById(R.id.search_magnifying_glass)
                .setOnClickListener(mSearchViewOnClickListener);
        mSearchEditTextLayout.findViewById(R.id.search_box_start_search)
                .setOnClickListener(mSearchViewOnClickListener);
        mSearchEditTextLayout.setOnBackButtonClickedListener(
                new SearchEditTextLayout.OnBackButtonClickedListener() {
                    @Override
                    public void onBackButtonClicked() {
                        onBackPressed();
                    }
                });
        mSearchEditTextLayout.setOnClearButtonViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMapFragment != null)
                    mMapFragment.clearMarkers();
            }
        });
        mShareLocationFab = (FloatingActionButton) findViewById(R.id.share_location_fab);
        mTransportationModes.get(0).setActivated(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,
                            android.support.v4.app.Fragment.instantiate(this,
                                    MapFragment.class.getName(), null),
                            MAP_FRAGMENT)
                    .commit();
        } else {
            mSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
            mInSearchUi = savedInstanceState.getBoolean(KEY_IN_SEARCH_UI);
            mActionBarController.restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACTS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                createChub(data);
            }
        }
    }

    private void createChub(final Intent data) {
            Map body = new HashMap<String, Object>();
            if (mDestination != null) {
                mRealm.beginTransaction();
                RealmRecentChub lastChub = mRealm.createObject(RealmRecentChub.class);
                RealmDestination realmDestination = mRealm.createObject(RealmDestination.class);
                realmDestination.setName(mDestination.name);
                realmDestination.setLatitude(mDestination.latitude);
                realmDestination.setLongitude(mDestination.longitude);
                realmDestination.setPlaceId(mDestination.id);
                lastChub.setDestination(realmDestination);
                mRealm.commitTransaction();
                body.put("destination", mDestination);
            }
            mChubApi.createChub(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext(refreshTokenAndRetry(mChubApi.createChub(body)))
                    .subscribe(
                            new Subscriber<Chub>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    new ErrorAction(ChubActivity.this).call(e);
                                }

                                @Override
                                public void onNext(Chub chub) {
                                    ChubLocationService.startLocationTracking(getApplicationContext(),
                                            chub.id);
                                    if (data != null && data.hasExtra("results")) {
                                        ArrayList<String> numbers = data.getStringArrayListExtra("results");

                                        SmsManager smsManager = SmsManager.getDefault();
                                        for (String number : numbers) {
                                            smsManager.sendTextMessage(number, null, getChubText(chub),
                                                    null, null);
                                        }

                                        Toast.makeText(ChubActivity.this, "Chub id " + chub.id,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChubActivity.this, R.string.contacts_error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
    }

    private <T> Func1<Throwable,? extends Observable<? extends T>> refreshTokenAndRetry(final Observable<T> toBeResumed) {
        return new Func1<Throwable, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(Throwable throwable) {
                // Here check if the error thrown really is a 401
                if (!(throwable instanceof RetrofitError))
                    return Observable.error(throwable);
                RetrofitError retrofitError = (RetrofitError) throwable;
                Response response = retrofitError.getResponse();
                if (RetrofitError.Kind.HTTP.equals(retrofitError.getKind()) &&
                        response != null &&
                        (HttpStatus.SC_FORBIDDEN == response.getStatus() ||
                                HttpStatus.SC_UNAUTHORIZED == response.getStatus())) {
                    return mChubApi.createToken(new HashMap()).flatMap(new Func1<AuthToken, Observable<? extends T>>() {
                        @Override
                        public Observable<? extends T> call(AuthToken token) {
                            mUserPreferences.getAuthTokenPreference().set(token.value);
                            return toBeResumed;
                        }
                    });
                }
                // re-throw this error because it's not recoverable from here
                return Observable.error(throwable);
            }
        };
    }

    private String getChubText(Chub chub) {
        boolean hasDestination = chub.destination != null;
        return hasDestination ?
                getString(R.string.chubbed_text_eta,
                        Uri.parse("http://chub.io/")
                                .buildUpon().appendPath(chub.publicId)) :
                getString(R.string.chubbed_text_location,
                        Uri.parse("http://chub.io/")
                                .buildUpon().appendPath(chub.publicId));

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SearchFragment) {
            mSearchFragment = (SearchFragment) fragment;
        } else if (fragment instanceof MapFragment) {
            mMapFragment = (MapFragment) fragment;
        }
    }

    /*
            * Listener used to send search queries to the phone search fragment.
            */
    private final TextWatcher mSearchQueryTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String newText = s.toString();
            if (newText.equals(mSearchQuery)) {
                // If the query hasn't changed (perhaps due to activity being destroyed
                // and restored, or user launching the same DIAL intent twice), then there is
                // no need to do anything here.
                return;
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onTextChange for mSearchView called with new query: " + newText);
                Log.d(TAG, "Previous Query: " + mSearchQuery);
            }
            mSearchQuery = newText;

            // Show search fragment only when the query string is changed to non-empty text.
            if (!TextUtils.isEmpty(newText)) {
                enterSearchUi(mSearchQuery);
            }

            if (mSearchFragment != null && mSearchFragment.isVisible()) {
                String location = null;
                if (mMapFragment != null) {
                    Location currentLocation = mMapFragment.getCurrentLocation();
                    if (currentLocation != null) {
                        location = String.format("%f,%f", currentLocation.getLatitude(),
                                currentLocation.getLongitude());
                    }
                }
                mSearchFragment.setQueryString(mSearchQuery, location);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    /**
     * Open the search UI when the user clicks on the search box.
     */
    private final View.OnClickListener mSearchViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isInSearchUi()) {
                mActionBarController.onSearchBoxTapped();
                enterSearchUi(mSearchView.getText().toString());
            }
        }
    };

    /**
     * Shows the search fragment
     */
    private void enterSearchUi(String query) {
        if (getFragmentManager().isDestroyed()) {
            // Weird race condition where fragment is doing work after the activity is destroyed
            // due to talkback being on (b/10209937). Just return since we can't do any
            // constructive here.
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Entering search UI");
        }
        mInSearchUi = true;
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        SearchFragment fragment = (SearchFragment) fragmentManager.findFragmentByTag(SEARCH_FRAGMENT);
        //transaction.setCustomAnimations(android.R.animator.fade_in, 0);
        if (fragment == null) {
            fragment = new SearchFragment();
            transaction.add(R.id.search_fragment_container, fragment, SEARCH_FRAGMENT);
        } else {
            transaction.show(fragment);
        }
        // DialtactsActivity will provide the options menu
        transaction.commit();
        //mListsFragment.getView().animate().alpha(0).withLayer();
    }

    /**
     * If the search term is empty and the user closes the soft keyboard, close the search UI.
     */
    private final View.OnKeyListener mSearchEditTextLayoutListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN &&
                    TextUtils.isEmpty(mSearchView.getText().toString())) {
                return maybeExitSearchUi();
            }
            return false;
        }
    };

    /**
     * @return True if the search UI was exited, false otherwise
     */
    private boolean maybeExitSearchUi() {
        if (isInSearchUi() && TextUtils.isEmpty(mSearchQuery)) {
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);
            return true;
        }
        return false;
    }

    /**
     * Hides the search fragment
     */
    private void exitSearchUi() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        // See related bug in enterSearchUI();

        if (fragmentManager.isDestroyed()) {
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(TAG, "exitSearchUi");

        mInSearchUi = false;

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentManager.findFragmentByTag(SEARCH_FRAGMENT));
        transaction.commit();

        //mListsFragment.getView().animate().alpha(1).withLayer();
        mActionBarController.onSearchUiExited();
    }

    @Override
    public void onBackPressed() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onBackPressed");
        if (isInSearchUi()) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onBackPressed in search ui");
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);
        } else {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "onBackPressed not in search ui");
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, mSearchQuery);
        outState.putBoolean(KEY_IN_SEARCH_UI, mInSearchUi);
        mActionBarController.saveInstanceState(outState);
    }

    public void onDestinationSelected(final GoogleAddress address) {
        mSearchView.setText(address.description);
        mSearchEditTextLayout.setCollapsedSearchBoxText(address.description);
        mMapFragment.clearMarkers();
        exitSearchUi();
        final Location currentLocation = mMapFragment.getCurrentLocation();
        //FIXME : pass current location instead of querying getCurrentLocation so
        //we can ensure we have a location
        if (currentLocation == null)
            return;
        mGeocodingService.getPlaceDetails(address.place_id, mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<GooglePlaceResponse<GooglePlace>,
                        Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                    @Override
                    public Observable<GoogleDirectionResponse<GoogleRoute>>
                    call(GooglePlaceResponse<GooglePlace> googlePlaceGooglePlaceResponse) {
                        LatLng destinationLatLng = new LatLng(googlePlaceGooglePlaceResponse.result.geometry.location.lat,
                                googlePlaceGooglePlaceResponse.result.geometry.location.lng);
                        mMapFragment.displayFlags(destinationLatLng);
                        mDestination = new Destination(
                                googlePlaceGooglePlaceResponse.result.id,
                                googlePlaceGooglePlaceResponse.result.name,
                                destinationLatLng.latitude,
                                destinationLatLng.longitude);
                        return mGeocodingService.getDirections(
                                String.format("%f,%f", currentLocation.getLatitude(),
                                        currentLocation.getLongitude()),
                                String.format("%f,%f", destinationLatLng.latitude,
                                        destinationLatLng.longitude),
                                mGoogleApiKey);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<GoogleDirectionResponse<GoogleRoute>>() {
                            @Override
                            public void call(GoogleDirectionResponse<GoogleRoute> googleRoute) {
                                mMapFragment.displayRoute(googleRoute.routes.get(0));
                            }
                        },
                        new ErrorAction(ChubActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mUserPreferences.getAuthTokenPreference().isSet()) {
            mChubApi.createToken(new HashMap()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<AuthToken>() {
                                @Override
                                public void call(AuthToken authToken) {
                                    mUserPreferences.getAuthTokenPreference().set(authToken.value);
                                }
                            },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            //silently fail
                        }
                    });
        }
    }

    @Override
    public boolean isInSearchUi() {
        return mInSearchUi;
    }

    @Override
    public boolean hasSearchQuery() {
        return !TextUtils.isEmpty(mSearchQuery);
    }

    @Override
    public boolean shouldShowActionBar() {
        return true;
    }

    @Override
    public int getActionBarHeight() {
        return mActionBarHeight;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUi(ChubLocationService.getCurrentlyTrackingChubId() != -1);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter(LOCATION_TRACKING_BROADCAST));
    }

    public void setupUi(boolean isTracking) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Setting floatingActionButton, is tracking " + isTracking);
        if (isTracking) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            mShareLocationFab.setColorNormalResId(R.color.chub_red);
            mShareLocationFab.setColorPressedResId(R.color.chub_dark_red);
            mShareLocationFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            mShareLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChubLocationService.stopLocationTracking(ChubActivity.this);
                }
            });
            mBottomLayout.setVisibility(View.VISIBLE);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            mBottomLayout.setVisibility(View.GONE);
            mShareLocationFab.setColorNormalResId(R.color.chub_blue);
            mShareLocationFab.setColorPressedResId(R.color.chub_dark_blue);
            mShareLocationFab.setImageResource(android.R.drawable.ic_menu_send);
            mShareLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(
                            new Intent(ChubActivity.this, ContactSelectionActivity.class),
                            PICK_CONTACTS);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @OnClick({ R.id.car_button, R.id.transit_button, R.id.bike_button, R.id.walk_button })
    public void onTransportationModeClick(ImageButton selectedMode) {
        ButterKnife.apply(mTransportationModes, SELECTED, selectedMode);
    }
}
