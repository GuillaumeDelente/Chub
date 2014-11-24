package android.chub.io.chub.fragment;

import android.chub.io.chub.R;
import android.chub.io.chub.data.api.model.GoogleRoute;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;


/**
 * Created by guillaume on 11/9/14.
 */
public class MapFragment extends BaseFragment {

    private static final String TAG = "MapFragment";
    private static final String SEARCH_FRAGMENT = "search_fragment";
    private MapView mMapView;
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

    public void displayRoute(GoogleRoute googleRoute) {
        if (mMap == null)
            return;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(PolyUtil.decode(googleRoute.overview_polyline.points));
        polylineOptions.width(getResources().getDimensionPixelSize(R.dimen.route_width));
        polylineOptions.color(getResources().getColor(R.color.route_color));
        mMap.addPolyline(polylineOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder()
                .include(new LatLng(googleRoute.bounds.southwest.lat,
                        googleRoute.bounds.southwest.lng))
                .include(new LatLng(googleRoute.bounds.northeast.lat,
                        googleRoute.bounds.northeast.lng)).build(),
                getResources().getDimensionPixelSize(R.dimen.map_bounds_padding)));
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        MapsInitializer.initialize(getActivity());
        mMap = mMapView.getMap();
        if (mMap != null) {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
