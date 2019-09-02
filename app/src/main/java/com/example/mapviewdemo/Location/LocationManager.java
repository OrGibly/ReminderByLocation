package com.example.mapviewdemo.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Manages location related actions.
 */
public class LocationManager {

    /**
     * Checks if the user gave this app location permission.
     * @return true if permission was given.
     */
    public static boolean checkLocationPermission(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the current location of the device.
     */
    public static void getCurLocationLatLng(Context context,
                                              final OnCurLocationReadyCallback onCurLocationReady) {
        if(checkLocationPermission(context)){
            FusedLocationProviderClient fusedLocationClient = LocationServices
                    .getFusedLocationProviderClient(context);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng latLanLocation = new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                onCurLocationReady.onReady(latLanLocation);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            onCurLocationReady.onReady(null);
                        }
                    });
        }
    }

    /**
     * An interface to a callback when current location info is ready.
     */
    public interface OnCurLocationReadyCallback {
        void onReady(@Nullable LatLng location);
    }
}
