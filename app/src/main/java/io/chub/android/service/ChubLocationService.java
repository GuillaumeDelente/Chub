package io.chub.android.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.chub.android.BuildConfig;
import io.chub.android.ChubApp;
import io.chub.android.R;
import io.chub.android.activity.ChubActivity;
import io.chub.android.data.api.ChubApi;
import io.chub.android.data.api.model.ChubLocation;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class ChubLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_START_TRACKING = "android.chub.io.service.action.TRACK_LOCATION";
    private static final String ACTION_STOP_TRACKING = "android.chub.io.service.action.STOP_TRACKING";
    private static final String KEY_CHUB_ID = "chub_id";
    private static final String KEY_DESTINATION_ID = "destination_id";
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final String TAG = "ChubLocationService";
    private static final int NOTIFICATION_ID = 1;
    private static final long LOCATIONS_POST_INTERVALL = 1000 * 10;
    private static long CURRENT_CHUB_ID = -1;
    private static String CURRENT_DESTINATION_ID = null;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private long mChubId;
    private final Handler mHandler = new Handler();
    private final IBinder mBinder = new ChubServiceBinder();
    private final Runnable mPostLocationsRunnable = new Runnable() {
        @Override
        public void run() {
            postLocations();
            mHandler.postDelayed(this, LOCATIONS_POST_INTERVALL);
        }
    };
    private Subscription mLocationPostSubscription;

    public class ChubServiceBinder extends Binder {
        public ChubLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChubLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final List<ChubLocation> mLocations = new ArrayList<>();
    @Inject
    ChubApi mChubApi;

    public static void startLocationTracking(Context context, long chubId, String destinationId) {
        Intent intent = new Intent(context, ChubLocationService.class);
        intent.putExtra(KEY_CHUB_ID, chubId);
        intent.putExtra(KEY_DESTINATION_ID, destinationId);
        intent.setAction(ACTION_START_TRACKING);
        context.startService(intent);
    }

    public static void stopLocationTracking(Context context) {
        Intent intent = new Intent(context, ChubLocationService.class);
        intent.setAction(ACTION_STOP_TRACKING);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_TRACKING.equals(action)) {
                handleActionTrackLocation(
                        intent.getLongExtra(KEY_CHUB_ID, -1),
                        intent.getStringExtra(KEY_DESTINATION_ID));
            } else if (ACTION_STOP_TRACKING.equals(action)) {
                handleActionStopTracking();
            }
        }
        return START_STICKY;
    }

    private void handleActionStopTracking() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Stop location tracking");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        mHandler.removeCallbacks(mPostLocationsRunnable);
        CURRENT_CHUB_ID = -1;
        CURRENT_DESTINATION_ID = null;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Resetting chub ID to " + CURRENT_CHUB_ID);
        sendLocalBroadcast(false);
        stopForeground(true);
        stopSelf();
    }

    private void sendLocalBroadcast(boolean isTracking) {
        Intent intent = new Intent(ChubActivity.LOCATION_TRACKING_BROADCAST);
        intent.putExtra(ChubActivity.TRACKING_LOCATION, isTracking);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrackLocation(long chubId, String destinationId) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Start location tracking");
        if (CURRENT_CHUB_ID != -1) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Could not start service as a Chub is already being shared");
            return;
        }
        ((ChubApp) getApplication()).inject(this);
        mLocations.clear();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mChubId = chubId;
        mGoogleApiClient.connect();
        CURRENT_CHUB_ID = chubId;
        CURRENT_DESTINATION_ID = destinationId;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Setting current chub ID to " + CURRENT_CHUB_ID);
        sendLocalBroadcast(true);
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {
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
        return builder.build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location service connected");
        mHandler.postDelayed(mPostLocationsRunnable, LOCATIONS_POST_INTERVALL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "Location changed " + location);
                        mLocations.add(new ChubLocation(location.getLatitude(),
                                location.getLongitude()));
                    }
                });
    }

    private void postLocations() {
        final List<ChubLocation> locations = new ArrayList<>(mLocations.size());
        synchronized (mLocations) {
            locations.addAll(mLocations);
            mLocations.clear();
        }
        mLocationPostSubscription = mChubApi.postLocation(mChubId, locations)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ChubLocation>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(),
                                "Error on postLocation",
                                Toast.LENGTH_SHORT).show();
                        //Failed to post locations, re-add failed locations
                        //to the buffer
                        mLocations.addAll(locations);
                    }

                    @Override
                    public void onNext(List<ChubLocation> chubLocations) {

                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failure by opening resolution activity
    }

    public static long getCurrentlyTrackingChubId() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "sending back chub ID " + CURRENT_CHUB_ID);
        return CURRENT_CHUB_ID;
    }

    public static String getCurrentlyDestinationId() {
        return CURRENT_DESTINATION_ID;
    }

    @Override
    public void onDestroy() {
        if (mLocationPostSubscription != null)
            mLocationPostSubscription.unsubscribe();
        Bugsnag.notify(new Exception("ChubLocationService onDestroy"));
        super.onDestroy();
    }
}
