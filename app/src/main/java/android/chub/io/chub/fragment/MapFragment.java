package android.chub.io.chub.fragment;

import android.chub.io.chub.R;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by guillaume on 11/9/14.
 */
public class MapFragment extends BaseFragment {

    private static final String TAG = "MapFragment";
    private static final String SEARCH_FRAGMENT = "search_fragment";
    private MapView mMapView;
    private ImageButton mShareLocationFab;
    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mShareLocationFab = (ImageButton) view.findViewById(R.id.share_location_fab);
        final int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
        mShareLocationFab.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, size, size);
            }
        });
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
        if (mMap == null)
            return null;
        Location location = mMap.getMyLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void displayFlags(LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .position(getCurrentLocation())
                .anchor(0, 1)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_flag)));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0, 1)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_stop)));
    }

    public void displayRoute(String polylines) {
        if (mMap == null)
            return;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(PolyUtil.decode(polylines));
        polylineOptions.width(getResources().getDimensionPixelSize(R.dimen.route_width));
        polylineOptions.color(getResources().getColor(R.color.route_color));
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        MapsInitializer.initialize(getActivity());
        mMap = mMapView.getMap();
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    mMap.setOnMyLocationChangeListener(null);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 15));
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void clearMarkers() {
        if (mMap == null)
            return;
        mMap.clear();
    }
}
