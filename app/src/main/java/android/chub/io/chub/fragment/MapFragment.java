package android.chub.io.chub.fragment;

import android.chub.io.chub.R;
import android.chub.io.chub.data.api.GeocodingService;
import android.chub.io.chub.data.api.model.GoogleAddress;
import android.chub.io.chub.data.api.model.GoogleResponse;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by guillaume on 11/9/14.
 */
public class MapFragment extends BaseFragment {

    private MapView mMapView;
    private ImageButton mShareLocationFab;
    private SearchView mSearchView;
    @Inject
    GeocodingService mGeocodingService;


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
        mSearchView = (SearchView) view.findViewById(R.id.search_view);
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
                        if (googleAddressGoogleResponse.results.isEmpty()) {
                            mSearchView.setQuery(null, false);
                        } else {
                            mSearchView.setQuery(
                                    googleAddressGoogleResponse.results.get(0).formattedAddress, false);
                        }
                    }
                });
            }

            @Override
            public void onZoom(ZoomEvent zoomEvent) {

            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
