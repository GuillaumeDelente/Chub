package io.chub.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.guillaumedelente.android.contacts.activities.ContactSelectionActivity;
import com.jakewharton.rxbinding.widget.RxRadioGroup;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
import io.chub.android.data.api.model.GoogleDurationResponse;
import io.chub.android.data.api.model.GooglePlace;
import io.chub.android.data.api.model.GooglePlaceResponse;
import io.chub.android.data.api.model.GoogleRoute;
import io.chub.android.data.api.model.RealmChub;
import io.chub.android.data.api.model.RealmChubs;
import io.chub.android.data.api.model.RealmContact;
import io.chub.android.data.api.model.RealmDestination;
import io.chub.android.data.api.model.RealmDestinations;
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
    public static final String LOCATION_TRACKING_STOPPED_BROADCAST = "location_tracking_broadcast";
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
    private RealmChub currentChub;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentChub = null;
            mMapFragment.clearMarkers();
            mSearchEditTextLayout.setCollapsedSearchBoxText(null);
            mSearchView.setText(null);
            setupUi(false);
        }
    };

    @InjectView(R.id.bottom_layout)
    View mBottomLayout;
    @InjectView(R.id.transport_radio_group)
    RadioGroup transportGroup;
    @InjectView(R.id.eta_textview)
    TextView etaTextView;
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
    Tracker googleAnalytics;
    Realm mRealm;
    private int mBottomLayoutHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        ButterKnife.inject(this);
        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        final Resources resources = getResources();
        setSupportActionBar(mToolbar);
        mRealm = Realm.getInstance(this);
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
                if (mMapFragment != null && currentChub == null) {
                    mMapFragment.clearMarkers();
                }
            }
        });
        mShareLocationFab = (FloatingActionButton) findViewById(R.id.share_location_fab);
        mLocationProvider = new ReactiveLocationProvider(this);
        RxRadioGroup
                .checkedChanges(transportGroup)
                .skip(1)
                /*
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .observeOn(mainThread())
                */
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer radioButtonId) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "On Checked Changed " + radioButtonId);
                        }
                        String travelMode;
                        switch (radioButtonId) {
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
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String travelMode) {
                        googleAnalytics.send(new HitBuilders.EventBuilder()
                                .setCategory("Chub")
                                .setAction("Change transport mode")
                                .setLabel(travelMode)
                                .build());
                    }
                })
                .flatMap(new Func1<String, Observable<DataHelper>>() {
                    @Override
                    public Observable<DataHelper> call(String transportationMode) {
                        if (currentChub != null) {
                            mRealm.beginTransaction();
                            currentChub.setTransportationMode(transportationMode);
                            mRealm.commitTransaction();
                            return Observable.just(new DataHelper(transportationMode,
                                    new LatLng(currentChub.getDestination().getLatitude(),
                                            currentChub.getDestination().getLongitude()),
                                    currentChub.getId()));
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<DataHelper, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(final DataHelper readOnlyChub) {
                        //Cannot use realm objects outside of created thread, using the dataHelper
                        //to avoid having to fetch the chub.
                        if (readOnlyChub == null) {
                            return Observable.just(null);
                        }
                        final HashMap<String, String> body = new HashMap<>();
                        body.put("travel_mode", readOnlyChub.travelMode);
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Changing transportation mode to " + readOnlyChub.travelMode);
                        }
                        return mChubApi
                                .updateChub(readOnlyChub.chubId, body)
                                .flatMap(new Func1<Chub, Observable<Void>>() {
                                    @Override
                                    public Observable<Void> call(Chub chub) {
                                        return displayRouteAndEtaObservable(readOnlyChub.destination,
                                                readOnlyChub.travelMode);
                                    }
                                })
                                .onErrorResumeNext(new Func1<Throwable, Observable<Void>>() {
                                    @Override
                                    public Observable<Void> call(Throwable throwable) {
                                        if (BuildConfig.DEBUG) {
                                            Log.d(TAG, "Error changing transportation mode");
                                            throwable.printStackTrace();
                                        }
                                        ErrorHandler.showError(ChubActivity.this, throwable);
                                        return Observable.empty();
                                    }
                                })
                                .unsubscribeOn(Schedulers.io());
                    }
                })
                .subscribeOn(mainThread())
                .observeOn(mainThread())
                .unsubscribeOn(mainThread())
                .subscribe(new Subscriber<Void>() {
                               @Override
                               public void onCompleted() {
                                   ErrorHandler.showError(ChubActivity.this, new Exception("onComplete"));
                               }

                               @Override
                               public void onError(Throwable e) {
                                   ErrorHandler.showError(ChubActivity.this, e);
                               }

                               @Override
                               public void onNext(Void unused) {

                               }
                           }
                );
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
            RealmDestinations.fromDestination(realmDestination, mDestination);
            realmDestination.setPlaceId(mDestination.id);
            lastChub.setDestination(realmDestination);
            for (int i = 0; i < numbers.size(); i++) {
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

    private void createChub(final Destination destination, final List<String> numbers) {
        HashMap body = new HashMap(1);
        if (destination != null) {
            body.put("destination", destination);
        }
        googleAnalytics.send(new HitBuilders.EventBuilder()
                .setCategory("Chub")
                .setAction("Start chub")
                .setLabel(destination == null ? "" : destination.name)
                .setValue(numbers.size())
                .build());
        mChubApi.createChub(body)
                .onErrorResumeNext(refreshTokenAndRetry(mChubApi.createChub(body)))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(mainThread())
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
                                mRealm.beginTransaction();
                                mRealm.where(RealmChub.class).findAll().clear();
                                RealmChub currentChub = mRealm.createObject(RealmChub.class);
                                RealmChubs.fromChub(mRealm, currentChub, chub);
                                if (destination != null) {
                                    //Place ID not being sent back from API
                                    currentChub.getDestination().setPlaceId(destination.id);
                                }
                                mRealm.commitTransaction();
                                ChubActivity.this.currentChub = currentChub;
                                setupUi(true);
                                ChubLocationService.startLocationTracking(
                                        getApplicationContext());
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
                if (HttpURLConnection.HTTP_FORBIDDEN == httpResponse ||
                        HttpURLConnection.HTTP_UNAUTHORIZED == httpResponse) {
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
                        Uri.parse("https://chub.io/")
                                .buildUpon().appendPath(chub.publicId)) :
                getString(R.string.chubbed_text_location,
                        Uri.parse("https://chub.io/")
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
            if (currentChub != null) {
                //For now, block any operation on the destination while chubbing.
                return;
            }
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
            if (!isInSearchUi() && currentChub == null) {
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
        googleAnalytics.send(new HitBuilders.EventBuilder()
                .setCategory("Search")
                .setAction("Enter search")
                .setLabel(query)
                .build());
        mShareLocationFab.setVisibility(View.GONE);
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
        mShareLocationFab.setVisibility(View.VISIBLE);
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
        displayDestination(destination.getPlaceId(), "driving");
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Getting route from recent chub selection");
        }
        List<String> numbers = new ArrayList<>(recentChub.getContacts().size());
        for (RealmContact contact : recentChub.getContacts()) {
            numbers.add(contact.getNumber());
        }
        createChub(new Destination(destination.getPlaceId(),
                destination.getName(),
                destination.getLatitude(),
                destination.getLongitude()), numbers);
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
        displayDestination(address.place_id, "driving");
        googleAnalytics.send(new HitBuilders.EventBuilder()
                .setCategory("Search")
                .setAction("Destination set")
                .setLabel(address.description)
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mUserPreferences.getAuthTokenPreference().isSet()) {
            mChubApi.createToken(new HashMap())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .subscribe(new Subscriber<AuthToken>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(AuthToken authToken) {
                            mUserPreferences.getAuthTokenPreference().set(authToken.value);
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
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter(LOCATION_TRACKING_STOPPED_BROADCAST));
        if (currentChub == null) {
            currentChub = mRealm.where(RealmChub.class).findFirst();
            if (currentChub != null && currentChub.getDestination() != null) {
                mSearchEditTextLayout.setCollapsedSearchBoxText(currentChub.getDestination().getName());
                displayDestination(currentChub.getDestination().getPlaceId(), currentChub.getTransportationMode());
            }
        }
        setupUi(currentChub != null);
    }

    private void displayDestination(final String destinationId, final String transportationMode) {
        getGooglePlace(destinationId)
                .flatMap(new Func1<GooglePlaceResponse<GooglePlace>,
                        Observable<Void>>() {
                    @Override
                    public Observable<Void>
                    call(GooglePlaceResponse<GooglePlace> googlePlaceGooglePlaceResponse) {
                        mDestinationLatLng =
                                new LatLng(googlePlaceGooglePlaceResponse.result.geometry.location.lat,
                                        googlePlaceGooglePlaceResponse.result.geometry.location.lng);
                        mDestination = new Destination(
                                destinationId,
                                googlePlaceGooglePlaceResponse.result.name,
                                mDestinationLatLng.latitude,
                                mDestinationLatLng.longitude);
                        return Observable.<Void>just(null)
                                .observeOn(mainThread())
                                .flatMap(new Func1<Void, Observable<Void>>() {
                                    @Override
                                    public Observable<Void> call(Void unused) {
                                        mMapFragment.displayFlags(mDestinationLatLng);
                                        return Observable.just(null);
                                    }
                                })
                                .observeOn(Schedulers.io())
                                .flatMap(new Func1<Void, Observable<Void>>() {
                                    @Override
                                    public Observable<Void> call(Void o) {
                                        if (BuildConfig.DEBUG) {
                                            Log.d(TAG, "Getting route from destination selection");
                                        }
                                        return displayRouteAndEtaObservable(mDestinationLatLng, transportationMode);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ErrorHandler.showError(ChubActivity.this, e);
                    }

                    @Override
                    public void onNext(Void result) {
                    }
                });
            final int buttonId;
            switch (transportationMode) {
                case ("transit"):
                    buttonId = R.id.transit_button;
                    break;
                case ("bicycling"):
                    buttonId = R.id.bike_button;
                    break;
                case ("walking"):
                    buttonId = R.id.walk_button;
                    break;
                default:
                case ("driving"):
                    buttonId = R.id.car_button;
                    break;
            }
            RxRadioGroup.checked(transportGroup).call(buttonId);
    }

    public void setupUi(boolean isTracking) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Setting floatingActionButton, is tracking " + isTracking);
        if (isTracking) {
            final Resources res = getResources();
            mShareLocationFab.setBackgroundTintList(ColorStateList.valueOf(res.getColor(R.color.chub_red)));
            mShareLocationFab.setRippleColor(res.getColor(R.color.chub_dark_red));
            mShareLocationFab.setImageResource(R.drawable.ic_close);
            mShareLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChubLocationService.stopLocationTracking(ChubActivity.this);
                    googleAnalytics.send(new HitBuilders.EventBuilder()
                            .setCategory("Chub")
                            .setAction("Stop chub")
                            .setLabel("From fab")
                            .build());
                }
            });
            if (currentChub.getDestination() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Window w = getWindow();
                    w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
                mBottomLayout.setVisibility(View.VISIBLE);
                if (mBottomLayoutHeight == 0) {
                    mBottomLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBottomLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mBottomLayoutHeight = mBottomLayout.getHeight();
                            setupUi(true);
                        }
                    });
                }
                FrameLayout.LayoutParams params =
                        ((FrameLayout.LayoutParams) mShareLocationFab.getLayoutParams());
                params.bottomMargin = mBottomLayout.getHeight() - mShareLocationFab.getHeight() / 2;
                mMapFragment.setMapBottomPadding(mBottomLayout.getHeight());
            } else {
                mBottomLayout.setVisibility(View.INVISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Window w = getWindow();
                    w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window w = getWindow();
                w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            mBottomLayout.setVisibility(View.INVISIBLE);
            FrameLayout.LayoutParams params =
                    ((FrameLayout.LayoutParams) mShareLocationFab.getLayoutParams());
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
            mMapFragment.resetMapBottomPadding();
            Resources res = getResources();
            mShareLocationFab.setBackgroundTintList(ColorStateList.valueOf(res.getColor(R.color.chub_blue)));
            mShareLocationFab.setRippleColor(res.getColor(R.color.chub_dark_blue));
            mShareLocationFab.setImageResource(R.drawable.ic_send);
            mShareLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    googleAnalytics.send(new HitBuilders.EventBuilder()
                            .setCategory("Contacts")
                            .setAction("Enter contacts")
                            .build());
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

    private Observable<GooglePlaceResponse<GooglePlace>> getGooglePlace(String placeId) {
        return mGeocodingService.getPlaceDetails(placeId, mGoogleApiKey);
    }

    private Observable<Void> displayRouteAndEtaObservable(LatLng destination, String travelMode) {
        return Observable.zip(
                getRouteObservable(destination, travelMode)
                        .observeOn(mainThread())
                        .flatMap(new Func1<GoogleRoute, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(GoogleRoute googleRoute) {
                                mMapFragment.displayRoute(googleRoute);
                                return Observable.just(null);
                            }
                        }),
                getEtaObservable(destination, travelMode)
                        .observeOn(mainThread())
                        .flatMap(new Func1<String, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(String duration) {
                                etaTextView.setText(duration);
                                return Observable.just(null);
                            }
                        }),
                new Func2<Void, Void, Void>() {
                    @Override
                    public Void call(Void unused, Void unused2) {
                        return null;
                    }
                });
    }

    private Observable<GoogleRoute> getRouteObservable(final LatLng destination,
                                                       final String travelMode) {
        return mLocationProvider.getLastKnownLocation()
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Location, Observable<GoogleDirectionResponse<GoogleRoute>>>() {
                    @Override
                    public Observable<GoogleDirectionResponse<GoogleRoute>> call(Location location) {
                        String origin = String.format("%f,%f", location.getLatitude(),
                                location.getLongitude());
                        String destinationStr = String.format("%f,%f",
                                destination.latitude,
                                destination.longitude);
                        return mGeocodingService.getDirections(
                                origin,
                                destinationStr,
                                travelMode,
                                mGoogleApiKey);
                    }
                })
                .flatMap(new Func1<GoogleDirectionResponse<GoogleRoute>, Observable<GoogleRoute>>() {
                    @Override
                    public Observable<GoogleRoute> call(GoogleDirectionResponse<GoogleRoute> googleRouteGoogleDirectionResponse) {
                        return Observable.just(googleRouteGoogleDirectionResponse.routes.get(0));
                    }
                });
    }

    private Observable<String> getEtaObservable(final LatLng destination,
                                                       final String travelMode) {
        return mLocationProvider.getLastKnownLocation()
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Location, Observable<GoogleDurationResponse>>() {
                    @Override
                    public Observable<GoogleDurationResponse> call(Location location) {
                        String origin = String.format("%f,%f", location.getLatitude(),
                                location.getLongitude());
                        String destinationStr = String.format("%f,%f",
                                destination.latitude,
                                destination.longitude);
                        return mGeocodingService.getDuration(
                                origin,
                                destinationStr,
                                travelMode,
                                mGoogleApiKey);
                    }
                })
                .flatMap(new Func1<GoogleDurationResponse, Observable<String>>() {
                    @Override
                    public Observable<String> call(GoogleDurationResponse duration) {
                        return Observable.just(duration.getDuration());
                    }
                });
    }

    private class DataHelper {
        public String travelMode;
        public LatLng destination;
        public long chubId;

        public DataHelper(String travelMode, LatLng destination, long chubId) {
            this.travelMode = travelMode;
            this.chubId = chubId;
            this.destination = destination;
        }
    }
}
