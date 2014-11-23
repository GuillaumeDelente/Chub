package android.chub.io.chub.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.chub.io.chub.ChubApp;
import android.chub.io.chub.R;
import android.chub.io.chub.activity.ChubActivity;
import android.chub.io.chub.data.api.ChubApi;
import android.chub.io.chub.data.api.model.ChubLocation;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
    private static final String ACTION_STOP_TRACKING = "android.chub.io.chub.service.action.STOP_TRACKING";
    private static final String KEY_CHUB_ID = "chub_id";
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final String TAG = "ChubLocationService";
    private static final int NOTIFICATION_ID = 1;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private long mChubId;
    @Inject
    ChubApi mChubApi;

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
            } else if (ACTION_STOP_TRACKING.equals(action)) {
                handleActionStopTracking();
            }
        }
        return START_STICKY;
    }

    private void handleActionStopTracking() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        Context context = getApplicationContext();
        Intent intent = new Intent(context, ChubLocationService.class);
        getApplicationContext().stopService(intent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrackLocation(long chubId) {
        Log.d(TAG, "Location connection");
        ((ChubApp) getApplication()).inject(this);
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
        displayNotification();
    }

    private void displayNotification() {
        Context context = getApplicationContext();
        Intent dismissIntent = new Intent(this, ChubLocationService.class);
        dismissIntent.setAction(ACTION_STOP_TRACKING);
        PendingIntent stopIntent = PendingIntent.getService(this, 0, dismissIntent, 0);

        Intent resultIntent = new Intent(this, ChubActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack
        stackBuilder.addParentStack(ChubActivity.class);
// Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
// Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Resources resources = context.getResources();
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_chub)
                .setContentTitle(resources.getString(R.string.you_are_chubing))
                .setContentText(resources.getString(R.string.location_shared))
                .setColor(resources.getColor(R.color.chub_blue))
                .setContentIntent(resultPendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_clear, getString(R.string.stop), stopIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location service connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "Location changed " + location);
                        mChubApi.postLocation(new ChubLocation(mChubId, location.getLatitude(),
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
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failure by opening resolution activity
    }
}
