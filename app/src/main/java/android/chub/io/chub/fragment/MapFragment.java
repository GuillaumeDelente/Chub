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

import com.mapbox.mapboxsdk.geometry.LatLng;
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
    private UserLocationOverlay mUserLocationOverlay;
    private GpsLocationProvider mGpsLocationProvider;
    private LatLng mUserLocation;

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
        mGpsLocationProvider = new GpsLocationProvider(getActivity());
        mUserLocationOverlay = new UserLocationOverlay(mGpsLocationProvider, mMapView);
        mUserLocationOverlay.enableMyLocation();
        mUserLocationOverlay.setDrawAccuracyEnabled(true);
        mMapView.getOverlays().add(mUserLocationOverlay);
        mUserLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mMapView.setCenter(mUserLocationOverlay.getMyLocation());
                mUserLocation = mUserLocationOverlay.getMyLocation();
            }
        });

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
    }

    public LatLng getCurrentLocation() {
        return mUserLocation;
    }
}
