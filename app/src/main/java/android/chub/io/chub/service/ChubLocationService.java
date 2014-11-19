package android.chub.io.chub.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class ChubLocationService extends IntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private static final String ACTION_TRACK_LOCATION = "android.chub.io.chub.service.action.TRACK_LOCATION";
    private static final String KEY_CHUB_ID = "chub_id";
    private static final int UPDATE_INTERVAL = 5;
    private static final int FASTEST_INTERVAL = 5;
    private static final String TAG = "ChubLocationService";
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
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
        mLocationClient = new LocationClient(getApplicationContext(), this, this);
        mLocationClient.connect();
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
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Location disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Location failed ");
        if (connectionResult.hasResolution()) {

            //TODO send back to the activity an error state
            /*
            try {
                // Start an Activity that tries to resolve the error

                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed " + location);
    }

    @Override
    public void onDestroy() {
        if (mLocationClient != null)
            mLocationClient.disconnect();
        super.onDestroy();
    }
}
