package com.example.mapviewdemo.Geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.mapviewdemo.Data.AlertData;
import com.example.mapviewdemo.Data.DataBaseManager;
import com.example.mapviewdemo.Location.LocationManager;
import com.example.mapviewdemo.Notification.AlertNotification;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class GeofencingManager {
    // Sets the geofence request not to notify at the moment
    // its added if the device is already in the geofence.
    private static final int NO_INITIAL_TRIGGER = 0;

    private Context context;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    /**
     * Constructor.
     * @param context The Context used in this instance.
     */
    public GeofencingManager(Context context){
        this.context = context;
        geofencingClient = LocationServices.getGeofencingClient(context);
    }

    /**
     * Registers GeofencingRequest according to an AlertData.
     * @param alertData The AlertData the represents the request.
     */
    public void registerGeofencingRequest(AlertData alertData){
        if(!LocationManager.checkLocationPermission(context)){
            Toast.makeText(context,"Error: no location permission. Alert will Not notify you."
                    ,Toast.LENGTH_LONG).show();
        }
            GeofencingRequest request = createGeofencingRequest(alertData);
            geofencingClient.addGeofences(request,getGeofencePendingIntent())
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"Error: some geofencing problem occurred." +
                            " Alert will Not notify you." +
                            "Try to reactivate the alert."
                            ,Toast.LENGTH_LONG).show();
                }
            })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Geofence was added."
                                    ,Toast.LENGTH_LONG).show();
                        }
                    });
    }

    /**
     * Unregisters GeofencingRequest.
     * @param alertData The AlertData the represents the request.
     */
    public void unregisterGeofencingRequest(AlertData alertData){
        ArrayList<String> geofencesRequestId = new ArrayList<>();
        geofencesRequestId.add(alertData.getId()+"");
        geofencingClient.removeGeofences(geofencesRequestId).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Geofence was NOT removed."
                        ,Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Geofence was removed."
                        ,Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handles the geofences that was triggered in the geofence transmission.
     * @param geofences The triggered geofences.
     */
    public void HandleTriggeringGeofences(List<Geofence> geofences){
        for (Geofence geofence:geofences){
            notifyUser(geofence);
        }


    }

    /**
     * Creates a GeofencingRequest from an AlertData.
     * @param alertData The AlertData to create the GeofencingRequest from.
     * @return The GeofencingRequest that was created.
     */
    private GeofencingRequest createGeofencingRequest(AlertData alertData){
        Geofence geofence = createGeofence(alertData);
        ArrayList<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence);
        return new GeofencingRequest.Builder()
                .setInitialTrigger(NO_INITIAL_TRIGGER)
                .addGeofences(geofences).build();
    }

    /**
     * Creates a Geofence fitting an AlertData.
     * @param alertData The AlertData that gives details how to create the fitting geofence.
     * @return The geofence that was created.
     */
    private Geofence createGeofence(AlertData alertData){
        return new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(alertData.getId()+"")
                .setCircularRegion(
                        alertData.getLocation().latitude,
                        alertData.getLocation().longitude,
                        alertData.getRange()*1000
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    /**
     * Returns a PendingIntent that starts a service when geofence transmissions are triggered.
     * @return PendingIntent for GeofenceTransitionsIntentService.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    /**
     * Tells the user that a geofence (which represents an Alert) was triggered.
     * @param triggerdGeofence The geofence that the user just entered.
     */
    private void notifyUser(Geofence triggerdGeofence){
        int alertId = Integer.parseInt(triggerdGeofence.getRequestId());
         AlertData alertData = DataBaseManager.getInstance().getDataSet().getById(alertId);
         new AlertNotification(context, alertData).notifyUser();
    }

}
