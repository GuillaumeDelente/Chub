package android.chub.io.chub.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class ChubLocationService extends IntentService implements
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_TRACK_LOCATION = "android.chub.io.chub.service.action.TRACK_LOCATION";
    private static final String KEY_CHUB_ID = "chub_id";
    private static final int UPDATE_INTERVAL = 5;
    private static final int FASTEST_INTERVAL = 5;
    private static final String TAG = "ChubLocationService";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private long mChubId;

    public ChubLocationService() {
        super("ChubService");
    }

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
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRACK_LOCATION.equals(action)) {
                handleActionTrackLocation(intent.getLongExtra(KEY_CHUB_ID, -1));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrackLocation(long chubId) {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mChubId = chubId;
        Log.d(TAG, "Location connection");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location service connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed " + location);
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failure by opening resolution activity
    }
}
