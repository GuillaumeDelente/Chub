package android.chub.io.chub.activity;

import android.accounts.AccountManager;
import android.chub.io.chub.BuildConfig;
import android.chub.io.chub.R;
import android.chub.io.chub.data.api.ApiKey;
import android.chub.io.chub.data.api.ChubService;
import android.chub.io.chub.data.api.GeocodingService;
import android.chub.io.chub.data.api.model.AuthToken;
import android.chub.io.chub.data.api.model.Chub;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleDirectionResponse;
import android.chub.io.chub.data.api.model.GooglePlace;
import android.chub.io.chub.data.api.model.GooglePlaceResponse;
import android.chub.io.chub.data.api.model.GoogleRoute;
import android.chub.io.chub.fragment.MapFragment;
import android.chub.io.chub.fragment.SearchFragment;
import android.chub.io.chub.service.ChubLocationService;
import android.chub.io.chub.util.DialerUtils;
import android.chub.io.chub.util.UserPreferences;
import android.chub.io.chub.widget.ActionBarController;
import android.chub.io.chub.widget.SearchEditTextLayout;
import android.content.res.Resources;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ChubActivity extends BaseActivity implements ActionBarController.ActivityUi {

    private static final String TAG = "ChubActivity";
    public static final String SEARCH_FRAGMENT = "search_fragment";
    public static final String MAP_FRAGMENT = "map_fragment";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_IN_SEARCH_UI = "in_search_ui";
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
    private ImageButton mShareLocationFab;
    @Inject
    GeocodingService mGeocodingService;
    @Inject
    @ApiKey
    String mGoogleApiKey;
    @Inject
    ChubService mChubService;
    @Inject
    UserPreferences mUserPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
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
        mShareLocationFab = (ImageButton) findViewById(R.id.share_location_fab);
        final int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
        mShareLocationFab.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, size, size);
            }
        });
        mShareLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChub();
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

    private void createChub() {
        if (mUserPreferences.getAuthTokenPreference().isSet()) {
            mChubService.createChub(new HashMap()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Chub>() {
                        @Override
                        public void call(Chub chub) {
                            Toast.makeText(ChubActivity.this, "Chub id " + chub.id,
                                    Toast.LENGTH_SHORT).show();
                            ChubLocationService.startLocationTracking(getApplicationContext(),
                                    chub.id);
                        }
                    });
        } else {
            mChubService.createToken(new HashMap()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<AuthToken>() {
                        @Override
                        public void call(AuthToken authToken) {
                            mUserPreferences.getAuthTokenPreference().set(authToken.value);
                        }
                    });
        }
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
                    LatLng position = mMapFragment.getCurrentLocation();
                    location = String.format("%f,%f", position.latitude,
                            position.longitude);
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
            transaction.add(R.id.fragment_container, fragment, SEARCH_FRAGMENT);
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

    public void onDestinationSelected(GoogleAddress address) {
        mSearchView.setText(address.description);
        mSearchEditTextLayout.setCollapsedSearchBoxText(address.description);
        mMapFragment.clearMarkers();
        exitSearchUi();
        LatLng currentLocation = mMapFragment.getCurrentLocation();
        mGeocodingService.getDirections(
                String.format("%f,%f", currentLocation.latitude,
                        currentLocation.longitude),
                address.description, mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GoogleDirectionResponse<GoogleRoute>>() {
                    @Override
                    public void call(GoogleDirectionResponse<GoogleRoute> googleAddressGoogleResponse) {
                        mMapFragment.displayRoute(googleAddressGoogleResponse.routes.get(0).overview_polyline.points);
                    }
                });
        mGeocodingService.getPlaceDetails(address.place_id, mGoogleApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GooglePlaceResponse<GooglePlace>>() {
                    @Override
                    public void call(GooglePlaceResponse<GooglePlace> place) {
                        LatLng latLng = new LatLng(place.result.geometry.location.lat,
                                place.result.geometry.location.lng);
                        mMapFragment.displayFlags(latLng);
                    }
                });

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
    public Toolbar getToolbar() {
        return mToolbar;
    }
}
