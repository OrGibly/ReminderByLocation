package com.example.reminderbylocation.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.reminderbylocation.App;
import com.example.reminderbylocation.Data.AlertData;
import com.example.locationbyreminder.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Visual Alert (a marker and a circle) on the map.
 */
public class Alert {
    // Marker hue (values: 0f-359f)
    private static final float MARKER_HUE = 201f;
    // Normal
    private static final float ALERT_ALPHA = 0.5f;
    private static final int CIRCLE_STROKE_COLOR;
    private static final int CIRCLE_FILL_COLOR;
    // Focused
    private static final float ALERT_ALPHA_FOCUSED = 0.8f;
    private static final int CIRCLE_STROKE_COLOR_FOCUSED;
    private static final int CIRCLE_FILL_COLOR_FOCUSED;
    // Being edited (edit mode)
    private static final float ALERT_ALPHA_EDIT_MODE = 0.8f;
    private static final int CIRCLE_STROKE_COLOR_EDIT_MODE;
    private static final int CIRCLE_FILL_COLOR_EDIT_MODE;
    //
    private static final float CIRCLE_STROKE_WIDTH = 2f;

    private boolean isFocused;
    private boolean isEditMode;

    private AlertData alertData;
    private GoogleMap googleMap;
    private Marker marker;
    //*** Circle radius measured in Meters but alert range in KM.
    private Circle circle;

    // Bundle keys.
    private static final String NAME_BUNDLE_KEY = "nameBundleKey";
    private static final String DETAILS_BUNDLE_KEY = "detailsBundleKey";
    private static final String LAT_BUNDLE_KEY = "latBundleKey";
    private static final String LNG_BUNDLE_KEY = "lanBundleKey";
    private static final String RANGE_BUNDLE_KEY = "rangeBundleKey";

    // Static initializer.
    // Initiates colors from resources file.
    static {
        Context context = App.getAppContext();
        CIRCLE_STROKE_COLOR = context.getResources().getColor(R.color.colorMapCircleStroke);
        CIRCLE_FILL_COLOR = context.getResources().getColor(R.color.colorMapCircleFill);
        CIRCLE_STROKE_COLOR_FOCUSED = context.getResources().getColor(R.color.colorMapCircleStrokeFocused);
        CIRCLE_FILL_COLOR_FOCUSED =context.getResources().getColor(R.color.colorMapCircleFillFocused);
        CIRCLE_STROKE_COLOR_EDIT_MODE = context.getResources().getColor(R.color.colorMapCircleStrokeEditMode);
        CIRCLE_FILL_COLOR_EDIT_MODE =context.getResources().getColor(R.color.colorMapCircleFillEditMode);
    }

    /**
     * Constructor.
     * @param alertData The alert data of the new created alert.
     * @param googleMap The bounded map of the Alert.
     */
    public Alert(AlertData alertData, GoogleMap googleMap) {
        this.alertData = alertData;
        this.googleMap = googleMap;
        this.marker = setMarker();
        this.marker.setTag(this);
        this.circle = setCircle();
        this.isFocused = false;
        this.isEditMode = false;
    }

    /**
     * Sets focused state and related visual features to the alert.
     * @param focus whether to focus or un-focus.
     * @param focusCamera weather to focus map camera on the alert.
     */
    public void setFocused(boolean focus, boolean focusCamera){
        isFocused = focus;
        if(isFocused){
            //edit mode look is prior to focused look.
            if(!isEditMode){
                setFocusedAppearance();
            }
            showInfoWindow();
        } else {
            //edit mode appearance is prior to normal appearance.
            if(!isEditMode){
                setNormalAppearance();
            }
            hideInfoWindow();
        }
        if(focusCamera)focusCamera();
    }

    /**
     * Sets edit mode and related visual features to the alert.
     * @param editMode whether to set edit mode or undo.
     */
    public void setEditMode(boolean editMode){
        isEditMode = editMode;
        if(isEditMode){
            setEditModeAppearance();
            showInfoWindow();
        } else {
            if(isFocused){
                setFocusedAppearance();
            }
            else {
                setNormalAppearance();
            }
        }
    }

    /**
     * Focus the map camera on the alert.
     */
    public void focusCamera(){
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()),500, null);
    }

    /**
     * Sets the normal appearance to the alert.
     */
    private void setNormalAppearance() {
        this.marker.setAlpha(ALERT_ALPHA);
        this.circle.setStrokeColor(CIRCLE_STROKE_COLOR);
        this.circle.setFillColor(CIRCLE_FILL_COLOR);
        this.circle.setStrokePattern(null);
    }

    /**
     * Sets the focused appearance to the alert.
     */
    private void setFocusedAppearance() {
        this.marker.setAlpha(ALERT_ALPHA_FOCUSED);
        this.circle.setStrokeColor(CIRCLE_STROKE_COLOR_FOCUSED);
        this.circle.setFillColor(CIRCLE_FILL_COLOR_FOCUSED);
        this.circle.setStrokePattern(null);
    }

    /**
     * Sets the edit mode appearance to the alert.
     */
    private void setEditModeAppearance() {
        this.getMarker().setAlpha(ALERT_ALPHA_EDIT_MODE);
        this.circle.setStrokeColor(CIRCLE_STROKE_COLOR_EDIT_MODE);
        this.circle.setFillColor(CIRCLE_FILL_COLOR_EDIT_MODE);
        List<PatternItem> pattern = Arrays.asList(new Dash(15), new Gap(10));
        this.circle.setStrokePattern(pattern);
    }

    /**
     * Sets the marker of the alert.
     * @return The marker that was set.
     */
    private Marker setMarker(){
        MarkerOptions markerOptions = new MarkerOptions();
        String name = alertData.getName().equals("")?" ":alertData.getName();
        markerOptions.alpha(ALERT_ALPHA)
                .position(alertData.getLocation())
                .title(name)
                .snippet(alertData.getDetails())
                .icon(BitmapDescriptorFactory.defaultMarker(MARKER_HUE));
        return googleMap.addMarker(markerOptions);
    }

    /**
     * Sets the circle of the alert.
     * @return The circle that was set.
     */
    private Circle setCircle(){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.strokeColor(CIRCLE_STROKE_COLOR)
                .strokeWidth(CIRCLE_STROKE_WIDTH)
                .fillColor(CIRCLE_FILL_COLOR)
                .radius(alertData.getRange()*1000)
                .center(alertData.getLocation());
        return googleMap.addCircle(circleOptions);
    }

    /**
     * Refreshes the info window of the alert.
     */
    public void refreshInfoWindow(){
        marker.showInfoWindow();
    }

    /**
     * Shows the info window of the alert.
     */
    public void showInfoWindow(){
        if(!marker.isInfoWindowShown())marker.showInfoWindow();
    }

    /**
     * Hides the info window of the alert.
     */
    public void hideInfoWindow(){
        if(marker.isInfoWindowShown())marker.hideInfoWindow();
    }

    /**
     * Alert's name getter.
     * @return The name of the alert.
     */
    public String getName(){
        return this.alertData.getName();
    }

    /**
     * AlertData getter.
     * @return The AlertData the represents the alert.
     */
    public AlertData getAlertData(){
        return this.alertData;
    }

    /**
     * Alert's detail getter.
     * @return The details of the alert.
     */
    public String getDetails(){
        return this.alertData.getDetails();
    }

    /**
     * Alert's name setter.
     * @param name The name to set.
     */
    public void setName(String name){
        this.alertData.setName(name);
        marker.setTitle(name);
    }

    /**
     * Alert's details setter.
     * @param details The details to set.
     */
    public void setDetails(String details){
        this.alertData.setDetails(details);
        marker.setSnippet(details);
    }

    /**
     * Alert's location setter.
     * @param location The location to set.
     */
    public void setLocation(LatLng location){
        this.getAlertData().setLocation(location);
        this.marker.setPosition(location);
        this.circle.setCenter(location);
    }

    /**
     * Alert's range setter.
     * @param range The range to set.
     */
    public void setRange(int range){
        alertData.setRange(range);
        circle.setRadius(range*1000);
    }

    /**
     *
     * @param alertData
     */
    public void setAlertData(@NonNull AlertData alertData){
        this.alertData = alertData;
    }

    public Marker getMarker() {
        return marker;
    }

    public Circle getCircle() {
        return circle;
    }

    public int getId(){return alertData.getId();}

    public int getRange(){
      return alertData.getRange();
    }

    public Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(NAME_BUNDLE_KEY,this.alertData.getName());
        bundle.putString(DETAILS_BUNDLE_KEY,this.alertData.getDetails());
        bundle.putDouble(LAT_BUNDLE_KEY,this.alertData.getLocation().latitude);
        bundle.putDouble(LNG_BUNDLE_KEY,this.alertData.getLocation().longitude);
        bundle.putInt(RANGE_BUNDLE_KEY,this.alertData.getRange());
        return bundle;
    }

    public void setBundle(Bundle alertBundle){
        setName(alertBundle.getString(NAME_BUNDLE_KEY));
        setDetails(alertBundle.getString(DETAILS_BUNDLE_KEY));
        double lat = alertBundle.getDouble(LAT_BUNDLE_KEY);
        double lng = alertBundle.getDouble(LNG_BUNDLE_KEY);
        setLocation(new LatLng(lat,lng));
        setRange(alertBundle.getInt(RANGE_BUNDLE_KEY));
    }

    public void setVisible(boolean visible){
        marker.setVisible(visible);
        circle.setVisible(visible);
        if(visible && isFocused)showInfoWindow();
    }

    public void removeFromMap(){
        marker.remove();
        circle.remove();
    }

}
