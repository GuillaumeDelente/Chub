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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import io.chub.android.BuildConfig;
import io.chub.android.R;
import io.chub.android.data.api.ApiKey;
import io.chub.android.data.api.ChubApi;
import io.chub.android.data.api.ErrorHandler;
import io.chub.android.data.api.GeocodingService;
import io.chub.android.data.api.model.AuthToken;
import io.chub.android.data.api.model.Chub;
import io.chub.android.data.api.model.Destination;
import io.chub.android.data.api.model.GoogleAddress;
import io.chub.android.data.api.model.GoogleDirectionResponse;
import io.chub.android.data.api.model.GooglePlace;
import io.chub.android.data.api.model.GooglePlaceResponse;
import io.chub.android.data.api.model.GoogleRoute;
import io.chub.android.data.api.model.RealmContact;
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
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import retrofit.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


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
    private LatLng mDestinationLatLng;
    private Destination mDestination;
    private ReactiveLocationProvider mLocationProvider;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(TRACKING_LOCATION)) {
                setupUi(intent.getBooleanExtra(TRACKING_LOCATION, false));
            }
        }
    };
    @InjectViews({R.id.car_button, R.id.transit_button, R.id.bike_button, R.id.walk_button})
    List<ImageButton> mTransportationModes;

    @InjectView(R.id.bottom_layout)
    View mBottomLayout;
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
        mLocationProvider = new ReactiveLocationProvider(this);
        mTransportationModes.get(0).setActivated(true);
        int transporationModesSize = mTransportationModes.size();
        ArrayList<Observable> mTransportationModesObservable =
                new ArrayList<>(transporationModesSize);
        for (View view : mTransportationModes) {
            mTransportationModesObservable.add(ViewObservable.clicks(view));
        }
        @SuppressWarnings("unchecked")
        final Observable<OnClickEvent>[] observables = mTransportationModesObservable
                .toArray((Observable<OnClickEvent>[]) new Observable[transporationModesSize]);
        Observable.merge(observables)
                .map(new Func1<OnClickEvent, OnClickEvent>() {
                    @Override
                    public OnClickEvent call(OnClickEvent onClickEvent) {
                        View selectedView = onClickEvent.view;
                        for (View v : mTransportationModes) {
                            v.setActivated(v.equals(selectedView));
                        }
                        return onClickEvent;
                    }
                })
                .throttleLast(1000, TimeUnit.MILLISECONDS, mainThread())
                .map(new Func1<OnClickEvent, String>() {
                    @Override
                    public String call(OnClickEvent onClickEvent) {
                        String travelMode;
                        switch (onClickEvent.view.getId()) {
                            case (R.id.transit_button):
                                travelMode = "transit";
                                break;
                            case (R.id.bike_button):
                                travelMode = "bicycling";
                                break;
                            case (R.id.walk_button):
                                travelMode = "walking";
                                break;
                            default:
                            case (R.id.car_button):
                                travelMode = "driving";
                                break;
                        }
                        return travelMode;
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Chub>>() {
                    @Override
                    public Observable<Chub> call(String travelMode) {
                        final HashMap<String, String> body = new HashMap<>();
                        body.put("travel_mode", travelMode);
                        return mChubApi.updateChub(
                                ChubLocationService.getChubId(), body)
                                .onErrorResumeNext(Observable.<Chub>empty());
                    }
                })
                .zipWith(mLocationProvider.getLastKnownLocation(),
                        new Func2<Chub, Location, Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                            @Override
                            public Observable<GoogleDirectionResponse<GoogleRoute>> call(Chub chub, Location location) {
                                return getRouteObservable(ChubLocationService.getDestinationLatLng(),
                                        location,
                                        chub.travelMode)
                                        .onErrorResumeNext(Observable.<GoogleDirectionResponse<GoogleRoute>>empty());
                            }
                        })
                .observeOn(mainThread())
                .flatMap(new Func1<Observable<GoogleDirectionResponse<GoogleRoute>>,
                        Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                    @Override
                    public Observable<GoogleDirectionResponse<GoogleRoute>>
                    call(Observable<GoogleDirectionResponse<GoogleRoute>> googleDirectionResponseObservable) {
                        return googleDirectionResponseObservable;
                    }
                })
                .subscribe(new Subscriber<GoogleDirectionResponse<GoogleRoute>>() {
                    @Override
                    public void onCompleted() {
                        ErrorHandler.showError(ChubActivity.this, new Exception("onComplete"));
                    }

                    @Override
                    public void onError(Throwable e) {
                        ErrorHandler.showError(ChubActivity.this, e);
                    }

                    @Override
                    public void onNext(GoogleDirectionResponse<GoogleRoute> googleRoute) {
                        mMapFragment.displayRoute(googleRoute.routes.get(0));
                    }
                });

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
                onContactSelected(data);
            }
        }
    }

    private void onContactSelected(final Intent data) {
        final ArrayList<String> numbers = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();
        if (data != null && data.hasExtra(ContactSelectionActivity.KEY_RESULT_NUMBERS)) {
            numbers.addAll(data.getStringArrayListExtra(ContactSelectionActivity.KEY_RESULT_NUMBERS));
            names.addAll(data.getStringArrayListExtra(ContactSelectionActivity.KEY_RESULT_NAMES));
        } else {
            Toast.makeText(ChubActivity.this, R.string.contacts_error,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (numbers.isEmpty()) {
            Toast.makeText(ChubActivity.this, R.string.no_contacts_selected,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDestinationLatLng != null) {
            mRealm.beginTransaction();
            RealmRecentChub lastChub = mRealm.createObject(RealmRecentChub.class);
            RealmDestination realmDestination = mRealm.createObject(RealmDestination.class);
            realmDestination.setName(mDestination.name);
            realmDestination.setLatitude(mDestination.latitude);
            realmDestination.setLongitude(mDestination.longitude);
            realmDestination.setPlaceId(mDestination.id);
            lastChub.setDestination(realmDestination);
            for (int i = 0; i < numbers.size(); i ++) {
                RealmContact realmContact = mRealm.createObject(RealmContact.class);
                realmContact.setNumber(numbers.get(i));
                realmContact.setName(names.get(i));
                lastChub.getContacts().add(realmContact);
            }
            lastChub.setLastUsed(Calendar.getInstance().getTimeInMillis());
            mRealm.commitTransaction();
            createChub(mDestination, numbers);
        } else {
            createChub(numbers);
        }
    }

    private void createChub(List<String> numbers) {
        createChub(null, numbers);
    }

    private void createChub(Destination destination, final List<String> numbers) {
        HashMap body = new HashMap(1);
        if (destination != null) {
            body.put("destination", destination);
        }
        mChubApi.createChub(body)
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .onErrorResumeNext(refreshTokenAndRetry(mChubApi.createChub(body)))
                .subscribe(
                        new Subscriber<Chub>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ErrorHandler.showError(ChubActivity.this, e);
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Error while creating the chub");
                                }
                            }

                            @Override
                            public void onNext(Chub chub) {
                                ChubLocationService.startLocationTracking(
                                        getApplicationContext(),
                                        chub.id,
                                        mDestinationLatLng);
                                SmsManager smsManager = SmsManager.getDefault();
                                for (String number : numbers) {
                                    if (BuildConfig.DEBUG) {
                                        Log.d(TAG, "Would have sent text to : " + number);
                                    } else {
                                        smsManager.sendTextMessage(number, null, getChubText(chub),
                                                null, null);
                                    }
                                }
                            }
                        });
    }


    private <T> Func1<Throwable, ? extends Observable<? extends T>> refreshTokenAndRetry(final Observable<T> toBeResumed) {
        return new Func1<Throwable, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(Throwable throwable) {
                // Here check if the error thrown really is a 401
                if (!(throwable instanceof HttpException))
                    return Observable.error(throwable);
                final int httpResponse = ((HttpException) throwable).code();
                if (HttpStatus.SC_FORBIDDEN == httpResponse ||
                        HttpStatus.SC_UNAUTHORIZED == httpResponse) {
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

    public void onRecentChubSelected(final RealmRecentChub recentChub) {
        final RealmDestination destination = recentChub.getDestination();
        mSearchView.setText(destination.getName());
        mSearchEditTextLayout.setCollapsedSearchBoxText(destination.getName());
        mMapFragment.clearMarkers();
        exitSearchUi();
        final Location currentLocation = mMapFragment.getCurrentLocation();
        //FIXME : pass current location instead of querying getCurrentLocation so
        //we can ensure we have a location
        if (currentLocation == null) {
            Toast.makeText(this, R.string.error_retry_destination, Toast.LENGTH_SHORT).show();
            return;
        }
        mRealm.beginTransaction();
        recentChub.setLastUsed(Calendar.getInstance().getTimeInMillis());
        mRealm.commitTransaction();
        mDestinationLatLng =
                new LatLng(destination.getLatitude(), destination.getLongitude());
        mMapFragment.displayFlags(mDestinationLatLng);
        mDestination = new Destination(
                destination.getPlaceId(),
                destination.getName(),
                mDestinationLatLng.latitude,
                mDestinationLatLng.longitude);
        getRouteObservable(mDestinationLatLng, currentLocation, "driving")
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(new Subscriber<GoogleDirectionResponse<GoogleRoute>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ErrorHandler.showError(ChubActivity.this, e);
                    }

                    @Override
                    public void onNext(GoogleDirectionResponse<GoogleRoute> result) {
                        mMapFragment.displayRoute(result.routes.get(0));
                        List<String> numbers = new ArrayList<>(recentChub.getContacts().size());
                        for (RealmContact contact : recentChub.getContacts()) {
                            numbers.add(contact.getNumber());
                        }
                        createChub(mDestination, numbers);
                    }
                });
    }

    public void onDestinationSelected(final GoogleAddress address) {
        mSearchView.setText(address.description);
        mSearchEditTextLayout.setCollapsedSearchBoxText(address.description);
        mMapFragment.clearMarkers();
        exitSearchUi();
        final Location currentLocation = mMapFragment.getCurrentLocation();
        //FIXME : pass current location instead of querying getCurrentLocation so
        //we can ensure we have a location
        if (currentLocation == null) {
            Toast.makeText(this, R.string.error_retry_destination, Toast.LENGTH_SHORT).show();
            return;
        }
        getGooglePlace(address.place_id)
                .flatMap(new Func1<GooglePlaceResponse<GooglePlace>,
                        Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                    @Override
                    public Observable<GoogleDirectionResponse<GoogleRoute>>
                    call(GooglePlaceResponse<GooglePlace> googlePlaceGooglePlaceResponse) {
                        mDestinationLatLng =
                                new LatLng(googlePlaceGooglePlaceResponse.result.geometry.location.lat,
                                        googlePlaceGooglePlaceResponse.result.geometry.location.lng);
                        mDestination = new Destination(
                                googlePlaceGooglePlaceResponse.result.id,
                                googlePlaceGooglePlaceResponse.result.name,
                                mDestinationLatLng.latitude,
                                mDestinationLatLng.longitude);
                        return Observable.just(null)
                                .flatMap(new Func1<Object, Observable<Void>>() {
                                    @Override
                                    public Observable<Void> call(Object o) {
                                        mMapFragment.displayFlags(mDestinationLatLng);
                                        return Observable.just(null);
                                    }
                                })
                                .observeOn(mainThread())
                                .subscribeOn(mainThread())
                                .flatMap(new Func1<Void, Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                                    @Override
                                    public Observable<GoogleDirectionResponse<GoogleRoute>> call(Void o) {
                                        return getRouteObservable(mDestinationLatLng,
                                                currentLocation, "driving");
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(new Subscriber<GoogleDirectionResponse<GoogleRoute>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ErrorHandler.showError(ChubActivity.this, e);
                    }

                    @Override
                    public void onNext(GoogleDirectionResponse<GoogleRoute> result) {
                        mMapFragment.displayRoute(result.routes.get(0));
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mUserPreferences.getAuthTokenPreference().isSet()) {
            mChubApi.createToken(new HashMap()).subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
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
        setupUi(ChubLocationService.getChubId() != -1);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter(LOCATION_TRACKING_BROADCAST));
    }

    public void setupUi(boolean isTracking) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Setting floatingActionButton, is tracking " + isTracking);
        if (isTracking) {
            mShareLocationFab.setColorNormalResId(R.color.chub_red);
            mShareLocationFab.setColorPressedResId(R.color.chub_dark_red);
            mShareLocationFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            mShareLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChubLocationService.stopLocationTracking(ChubActivity.this);
                }
            });
            if (ChubLocationService.getDestinationLatLng() != null) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                mBottomLayout.setVisibility(View.VISIBLE);
            }
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

    private Observable<GoogleDirectionResponse<GoogleRoute>> getRouteObservable(LatLng destination,
                                                                                final Location location,
                                                                                String travelMode) {
        return mGeocodingService.getDirections(
                String.format("%f,%f", location.getLatitude(),
                        location.getLongitude()),
                String.format("%f,%f", destination.latitude,
                        destination.longitude),
                travelMode,
                mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread());
    }

    private Observable<GooglePlaceResponse<GooglePlace>> getGooglePlace(String placeId) {
        return mGeocodingService.getPlaceDetails(placeId, mGoogleApiKey);
    }
}
