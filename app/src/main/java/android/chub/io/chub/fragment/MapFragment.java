package android.chub.io.chub.fragment;

import android.chub.io.chub.R;
import android.chub.io.chub.data.api.GeocodingService;
import android.chub.io.chub.widget.ActionBarController;
import android.content.res.Resources;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import javax.inject.Inject;

/**
 * Created by guillaume on 11/9/14.
 */
public class MapFragment extends BaseFragment {

    private static final String TAG = "MapFragment";
    private static final String SEARCH_FRAGMENT = "search_fragment";
    private MapView mMapView;
    private ImageButton mShareLocationFab;
    private ActionBarController mActionBarController;
    private EditText mSearchView;
    //private SearchEditTextLayout mSearchView;
    @Inject
    GeocodingService mGeocodingService;
    private int mActionBarHeight;
    private String mSearchQuery;
    private boolean mInRegularSearch;
    private FrameLayout mParentLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        mShareLocationFab = (ImageButton) view.findViewById(R.id.share_location_fab);
        //mSearchView = (SearchEditTextLayout) view.findViewById(R.id.search_view);
        final int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
        mShareLocationFab.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, size, size);
            }
        });
        GpsLocationProvider myLocationProvider = new GpsLocationProvider(getActivity());
        final UserLocationOverlay myLocationOverlay = new UserLocationOverlay(myLocationProvider, mMapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mMapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mMapView.setCenter(myLocationOverlay.getMyLocation());
            }
        });

        mParentLayout = (FrameLayout) view.findViewById(R.id.fragment_container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Resources resources = getActivity().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            ((FrameLayout.LayoutParams) mShareLocationFab.getLayoutParams()).bottomMargin =
                    resources.getDimensionPixelSize(resourceId)
                            + resources.getDimensionPixelSize(R.dimen.fab_margin);
        }
        /*
        mMapView.addListener(new MapListener() {
            @Override
            public void onScroll(ScrollEvent scrollEvent) {
                LatLng center = mMapView.getCenter();
                mGeocodingService.getAddress(String.format("%f,%f", center.getLatitude(),
                        center.getLongitude()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<GoogleResponse<GoogleAddress>>() {
                            @Override
                            public void call(GoogleResponse<GoogleAddress> googleAddressGoogleResponse) {
                                searchEditTextLayout.setCollapsedSearchBoxText(null);
                                if (!googleAddressGoogleResponse.results.isEmpty()) {
                                    searchEditTextLayout
                                            .setCollapsedSearchBoxText(
                                                    googleAddressGoogleResponse
                                                            .results.get(0).formattedAddress);
                                }
                            }
                        });
            }

            @Override
            public void onZoom(ZoomEvent zoomEvent) {

            }
        });
        */
    }
}
