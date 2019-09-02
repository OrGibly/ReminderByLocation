package com.example.reminderbylocation.Geofence;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * A Service which handles the geofence transitions.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "Geofence Error";
    private static final String name = "GeofenceService";

    /**
     * Constructor.
     */
    public GeofenceTransitionsIntentService() {
        super(name);
    }

    /**
     * Handles the geofence transitions.
     * @param intent contains the geofence event.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "geofencing event error.");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            new GeofencingManager(this).HandleTriggeringGeofences(triggeringGeofences);
        }
    }
}
