package android.chub.io.chub.service;

import android.app.IntentService;
import android.app.Service;
import android.chub.io.chub.ChubApp;
import android.chub.io.chub.data.api.ChubService;
import android.chub.io.chub.data.api.model.ChubLocation;
import android.chub.io.chub.data.api.model.Destination;
import android.chub.io.chub.data.api.model.GooglePlace;
import android.chub.io.chub.data.api.model.GooglePlaceResponse;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class ChubLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_TRACK_LOCATION = "android.chub.io.chub.service.action.TRACK_LOCATION";
    private static final String KEY_CHUB_ID = "chub_id";
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final String TAG = "ChubLocationService";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private long mChubId;
    @Inject
    ChubService mChubService;

    /**
     * Starts this service to perform location tracking. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startLocationTracking(Context context, long chubId) {
        Intent intent = new Intent(context, ChubLocationService.class);
        intent.putExtra(KEY_CHUB_ID, chubId);
        intent.setAction(ACTION_TRACK_LOCATION);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRACK_LOCATION.equals(action)) {
                handleActionTrackLocation(intent.getLongExtra(KEY_CHUB_ID, -1));
            }
        }
        return START_STICKY;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrackLocation(long chubId) {
        Log.d(TAG, "Location connection");
        ((ChubApp) getApplicationContext()).inject(this);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mChubId = chubId;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location service connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "Location changed " + location);
                        mChubService.postLocation(new ChubLocation(mChubId, location.getLatitude(),
                                location.getLongitude())).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<ChubLocation>() {
                                    @Override
                                    public void call(ChubLocation place) {

                                    }
                                });
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failure by opening resolution activity
    }
}
