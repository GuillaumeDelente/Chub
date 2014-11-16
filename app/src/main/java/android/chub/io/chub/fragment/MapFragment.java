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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_flag)));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_stop)));
    }

    public void displayRoute(String polylines) {
        if (mMap == null)
            return;
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(decodePoly(polylines));
        polylineOptions.width(14);
        polylineOptions.color(getResources().getColor(R.color.route_color));
        mMap.addPolyline(polylineOptions);
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
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
}
