package com.example.reminderbylocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.reminderbylocation.Data.AlertData;
import com.example.reminderbylocation.Data.DataBaseManager;
import com.example.reminderbylocation.Geofence.GeofencingManager;
import com.example.reminderbylocation.Location.LocationManager;
import com.example.reminderbylocation.Map.Alert;
import com.example.reminderbylocation.Map.AlertMiniEditorFragment;
import com.example.locationbyreminder.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;


public class AlertsMapFragment extends Fragment implements Updateable {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DataBaseManager.DataSet alertsDataSet;
    //An alert that has got no id (apparently not is DB) will have NULL_ID.
    private int NULL_ID = DataBaseManager.NULL_ID;
    //Initialized with setAlertsToMap() return value.
    //(correspondingly with alertsDataSet).
    private List<Alert> alerts;

    private GeofencingManager geofencingManager;

    //Used to save and restore states by Bundle.
    private static final String MAP_VIEW_BUNDLE_KEY = "mapViewBundleKey";
    private static final String FOCUSED_ALERT_ID_KEY = "focusedAlertIdKey";
    private static final String EDIT_MODE_KEY = "editModeKey";
    private static final String STARTED_FOR_EDIT_MODE_ONLY_KEY = "startedForEditModeOnlyKey";
    private static final String ALERT_TO_EDIT_ID_KEY = "alertToEditIdKey";
    private static final String NEW_ALERT_TO_EDIT_BUNDLE_KEY = "newAlertToEditBundleKey";
    private static final String TEMP_ALERT_TO_EDIT_BUNDLE_KEY = "tempAlertToEditBundleKey";

    private MapView mapView;
    private Bundle savedInstanceState;

    //The last alert that the user clicked on.
    //Set with setFocusedAlert();
    private Alert focusedAlert;

    //***EDIT MODE***.
    //Indicates if an alert is being edited or if to start editing an
    //alert(specified by alertToEditId) in case of restoring the fragment.
    private boolean editMode;
    //indicates if to start the fragment in editMode with a specific alert
    //and close it immediately when editMode is finished.
    //*** DO NOT use AlertsMapFragment.startToEditLocationAndRangeOnly() if the fragment
    //    is not free to be added or removed(like in view pager).
    private boolean startToEditLocationAndRangeOnly = false;
    //* Given as an int (out of Life Cycle in AlertsMapFragment.startToEditLocationAndRangeOnly())
    //  when need to start the fragment for editing only
    //  or initialized when the fragment is being restored, in order to restore edit mode.
    private int alertToEditId;
    //The alert that was chosen to be edited is being edited in edit mode.
    //* null * - if edit mode is set for creating a new alert.
    private Alert alertToEdit;
    //Temporary Alert for editing process - represents pending changes in alertToEdit.
    //*** Not saved in DB, hence has no id(NULL_ID).
    //*** Do Not set tempAlertToEdit into focusedAlert (focusedAlert=tempAlertToEdit),
    //    because in case of fragment restoration tempAlertToEdit has no id, therefore can not
    //    be restored. instead set focusedAlert=alertToEdit but visually focus on tempAlertToEdit.
    private Alert tempAlertToEdit;
    //if started to edit location and range only, update this fragment if changes was applied.
    private AlertEditorFragment editorFragment;

    //edit mode widgets.
    private EditButton editButton;
    private AlertRangeBar alertRangeBar;
    private ApplyButton applyButton;
    private CancelButton cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askForLocationPermission();
        alertsDataSet = DataBaseManager.getInstance().getDataSet();
        geofencingManager = new GeofencingManager(getContext());
        this.savedInstanceState = savedInstanceState;
        if(savedInstanceState==null){
            if(!startToEditLocationAndRangeOnly){
                //if shouldn't restore state (and edit mode)
                //and not started to edit location and range only.
                alertToEditId = NULL_ID;
            }
            editMode = false;
        } else {
            //restore instance state.
            //fragment state will be fully restored after restoreMapStateAfterMapIsReady().
            startToEditLocationAndRangeOnly = savedInstanceState.getBoolean(
                    STARTED_FOR_EDIT_MODE_ONLY_KEY);
            if(startToEditLocationAndRangeOnly){
                editorFragment = getEditorFragment();
            }
            editMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
            if(editMode)alertToEditId = savedInstanceState.getInt(ALERT_TO_EDIT_ID_KEY);
            else alertToEditId = NULL_ID;
        }
    }

    private AlertEditorFragment getEditorFragment(){
        for (Fragment fragment: getFragmentManager().getFragments()) {
            if(fragment instanceof AlertEditorFragment){
                return (AlertEditorFragment)fragment;
            }
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
        //save focused alert.
        if(focusedAlert!=null){
            int focusedAlertId = focusedAlert.getId();
            outState.putInt(FOCUSED_ALERT_ID_KEY,focusedAlertId);
        }
        //save edit mode.
        if(editMode){
            outState.putBoolean(EDIT_MODE_KEY,true);
            outState.putBoolean(STARTED_FOR_EDIT_MODE_ONLY_KEY, startToEditLocationAndRangeOnly);
            alertToEditId = alertToEdit.getId();
            if(alertToEditId==NULL_ID){
                //editing a new alert (not in DB yet).
                outState.putBundle(NEW_ALERT_TO_EDIT_BUNDLE_KEY,tempAlertToEdit.getBundle());
            }
            outState.putInt(ALERT_TO_EDIT_ID_KEY,alertToEditId);
            outState.putBundle(TEMP_ALERT_TO_EDIT_BUNDLE_KEY,tempAlertToEdit.getBundle());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView= inflater.inflate(R.layout.fragment_alerts_map,container,
                false);
        // *** IMPORTANT ***
        //MapView requires that the bundle you pass
        //contains only MapView SDK objects or sub-bundles.
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView = contentView.findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        //Initialize Map.
        mapView.getMapAsync(initializeMapCallback);
        return contentView;
    }

    private void askForLocationPermission(){
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        try {
                            googleMap.setMyLocationEnabled(true);
                        } catch (SecurityException e){}
                    }
                });
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Permission needed!")
                        .setMessage("This App needs your location permission.")
                        .create().show();
            }
        }
    }

    private OnMapReadyCallback initializeMapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            alerts = setAlertsOnMap(googleMap,alertsDataSet);
            if(savedInstanceState==null){
                if(startToEditLocationAndRangeOnly){
                    alertToEdit = getAlertById(alertToEditId);
                    startEditMode(alertToEdit);
                    moveCamera(googleMap,alertToEdit.getAlertData().getLocation());
                }else {
                    // moves camera to current location.
                    LocationManager.getCurLocationLatLng(getContext(),
                            new LocationManager.OnCurLocationReadyCallback() {
                        @Override
                        public void onReady(@Nullable LatLng location) {
                            if(location != null){
                                moveCamera(googleMap, location);
                            }
                        }
                    });
                }
            } else {
                //restores map state if needed.
                restoreMapStateAfterMapIsReady(googleMap);
            }
            setMapCallbacks(googleMap);
        }
    };


    private void restoreMapStateAfterMapIsReady(GoogleMap googleMap){
        if(savedInstanceState!=null){
            //restore edit mode.
            if(editMode){
                alertToEdit = getAlertById(alertToEditId);
                if(alertToEdit==null){ // new alert being edited.
                    Bundle alertBundle = savedInstanceState.getBundle(
                            TEMP_ALERT_TO_EDIT_BUNDLE_KEY);
                    alertToEdit = addAlert(googleMap, alertBundle,false);
                }
                startEditMode(alertToEdit);
            }
            //restore focused alert.
            int focusedAlertId = savedInstanceState.getInt(FOCUSED_ALERT_ID_KEY);
            if(!(focusedAlertId==alertToEditId)){
                setFocusedAlert(getAlertById(focusedAlertId),false);
            }
            //else: focused alert will be restored in startEditModeCallback.
            //      because tempAlertToEdit might be still null.
        }
    }

    private void setMapCallbacks(final GoogleMap googleMap){

        //onMapClick.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(editMode){
                    tempAlertToEdit.setLocation(latLng);
                    setFocusedAlert(tempAlertToEdit,true);
                }else {
                    setFocusedAlert(null,false);
                }
            }
        });

        //onMapLongClick.
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(!editMode){
                    AlertMiniEditorFragment fragment = new AlertMiniEditorFragment();
                    fragment.bindAlertsMapFragment(AlertsMapFragment.this);
                    fragment.createNewAlert(latLng);
                    getFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.mainContainer,fragment)
                            .commit();
                }
            }
        });

        //onMarkerClick.
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                setFocusedAlert((Alert)marker.getTag(),true);
                return true;
            }
        });

        //onInfoWindowClick.
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(editMode){
                    if(marker.getTag()==tempAlertToEdit){
                        AlertMiniEditorFragment fragment = new AlertMiniEditorFragment();
                        fragment.bindAlertsMapFragment(AlertsMapFragment.this);
                        getFragmentManager()
                                .beginTransaction()
                                .addToBackStack(null)
                                .add(R.id.mainContainer, fragment)
                                .commit();
                    }
                }
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        alertRangeBar = new AlertRangeBar();
        editButton = new EditButton();
        applyButton = new ApplyButton();
        cancelButton = new CancelButton();
    }

    private List<Alert> setAlertsOnMap(GoogleMap googleMap, DataBaseManager.DataSet dataSet){
        googleMap.clear();
        List<Alert> alerts = new ArrayList<>();
        for(int i = 0; i< dataSet.size(); i++){
            AlertData alertData = dataSet.get(i);
            Alert alert = addAlert(googleMap,alertData,false);
            alerts.add(alert);
        }
        return alerts;
    }

    private Alert addAlert(GoogleMap googleMap,AlertData alertData,boolean focus){
        Alert alert = new Alert(alertData,googleMap);
        if(focus)setFocusedAlert(alert,true);
        return alert;
    }

    private Alert addAlert(GoogleMap googleMap,Bundle alertBundle ,boolean focus){
        AlertData alertData = new AlertData("",new LatLng(0,0),0,"",
                false);
        Alert alert = new Alert(alertData,googleMap);
        alert.setBundle(alertBundle);
        if(focus)setFocusedAlert(alert,true);
        return alert;
    }

    private Alert getAlertById(int id){
        if(alerts==null)return null;
        for (Alert alert: alerts) {
            if(alert.getId()==id)return alert;
        }
        return null;
    }

    private void setFocusedAlert(@Nullable Alert alert, boolean focusCamera){
        //cancel last focused alert.
        if(focusedAlert!=null){
            editButton.hide(true);
            if(focusedAlert==alertToEdit){
                //alertToEdit is invisible in edit mode, so change tempAlertToEdit.
                tempAlertToEdit.setFocused(false,false);
            } else {
                focusedAlert.setFocused(false,false);
            }
            focusedAlert=null;
        }

        //set focused alert.
        if(alert!=null){
            if(alert==tempAlertToEdit || alert==alertToEdit){
                //alertToEdit is invisible in edit mode, so change tempAlertToEdit.
                tempAlertToEdit.setFocused(true,focusCamera);
                focusedAlert = alertToEdit;
            } else {
                alert.setFocused(true,focusCamera);
                focusedAlert = alert;
                if(!editMode){
                    editButton.show(true);
                }
            }
        }
    }

    private void moveCamera(GoogleMap googleMap, LatLng latLng){
        LatLng latLngSouthwestBound = new LatLng(latLng.latitude-0.5,latLng.longitude-0.5);
        LatLng latLngNortheastBound = new LatLng(latLng.latitude+0.5,latLng.longitude+0.5);
        LatLngBounds latLngBounds = new LatLngBounds(latLngSouthwestBound,latLngNortheastBound);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
    }

    @Override
    public void update() {
        mapView.getMapAsync(onUpdateMap);
        if(editMode)finishEditMode(false,false);
        editButton.hide(false);
    }

    private OnMapReadyCallback onUpdateMap = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            alerts = setAlertsOnMap(googleMap,alertsDataSet);
            LocationManager.getCurLocationLatLng(getContext(),
                    new LocationManager.OnCurLocationReadyCallback() {
                @Override
                public void onReady(@Nullable LatLng location) {
                    if(location != null){
                        moveCamera(googleMap, location);
                    }
                }
            });
        }
    };

//---------------------*** EditMode related methods ***-----------------------

    public void startToEditLocationAndRangeOnly(int alertToEditId,
                                                AlertEditorFragment fragmentToUpdate){
        startToEditLocationAndRangeOnly = true;
        this.alertToEditId = alertToEditId;
        this.editorFragment = fragmentToUpdate;
    }

    public void startEditModeWithNewAlert(final AlertData alertData){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                alertToEdit = addAlert(googleMap,alertData,true);
                startEditMode(alertToEdit);
            }
        });
    }

    private void startEditMode(@NonNull Alert alert){
        editMode=true;
        //AlertToEdit will be temporarily replaced with a temporary alert (tempAlertToEdit)
        //until finishEditMode() will be called.
        editButton.hide(true);
        alertToEdit = alert;
        alertToEdit.setVisible(false);//if new alert created.
        mapView.getMapAsync(startEditModeCallback);
    }

    OnMapReadyCallback startEditModeCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //replace the alertToEdit with a copy.
            AlertData tempAlertData = new AlertData(alertToEdit.getAlertData());
            tempAlertToEdit = addAlert(googleMap,tempAlertData,false);
            tempAlertToEdit.setEditMode(true);
            //restore focusedAlert and tempAlertToEdit here,
            //because tempAlertToEdit is null before calling this method.
            //*** savedInstanceState is set to be null when user presses the
            //    edit button(which starts edit mode), because it stays NOT null
            //    even after fragment restoration, and in order to prevent unwanted
            //    restoration which causes bugs.
            if(savedInstanceState!=null){
                Bundle bundle = savedInstanceState.getBundle(TEMP_ALERT_TO_EDIT_BUNDLE_KEY);
                if(bundle!=null){
                    tempAlertToEdit.setBundle(bundle);
                }
                int focusedAlertId = savedInstanceState.getInt(FOCUSED_ALERT_ID_KEY);
                if(focusedAlertId==alertToEditId){
                    setFocusedAlert(tempAlertToEdit,false);
                }
            }
            //animate only if edit mode isn't started right when opening the fragment
            //like when starting for edit mode only or restoring the last edit mode.
            boolean animate = !startToEditLocationAndRangeOnly && savedInstanceState==null;
            alertRangeBar.show(animate);
            applyButton.show(animate);
            cancelButton.show(animate);
        }
    };

    private void finishEditMode(boolean applyChanges,boolean animate){
        if(!editMode)return;

        if(applyChanges){
            alertToEdit.setBundle(tempAlertToEdit.getBundle());
            AlertData alertToEditData = alertToEdit.getAlertData();
            if(alertToEdit.getId()==NULL_ID) {//new alert.
                alertsDataSet.add(alertToEditData);
            }
            alertToEdit.setVisible(true);

            // Create a new geofence.
            if(alertToEdit.getAlertData().isOn()){
                geofencingManager.registerGeofencingRequest(alertToEditData);
            }

        } else {//if changes canceled:
            if(alertToEdit.getId()==NULL_ID){//new alert.
                alertToEdit.removeFromMap();
            } else {
                alertToEdit.setVisible(true);
            }
        }

        tempAlertToEdit.removeFromMap();
        tempAlertToEdit=null;

        alertToEdit=null;
        alertToEditId = NULL_ID;

        editMode=false;
        alertRangeBar.hide(animate);
        applyButton.hide(animate);
        cancelButton.hide(animate);
        //this part of code is relevant only if the
        //fragment is independent(not in viewPager).
        if(startToEditLocationAndRangeOnly){
            startToEditLocationAndRangeOnly =false;
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

//-------------------------------------------------------------------------

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public Alert getTempAlertToEdit() {
        return this.tempAlertToEdit;
    }

//------------------------------- UI components as inner classes -----------------------
    //Instantiate only after fragment's view is created.
    private class AlertRangeBar{
        private final long MOTION_DURATION = 300;
        LinearLayout container;
        SeekBar seekBar;
        TextView rangeIndicator;

         AlertRangeBar(){
            View view = getView();
            container = view.findViewById(R.id.rangeBarContainer);
            seekBar = view.findViewById(R.id.seekBarRange);
            rangeIndicator = view.findViewById(R.id.textViewRangeIndicator);
         }

         public void show(boolean animate){
             seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
             if(tempAlertToEdit!=null){
                 alertRangeBar.seekBar.setProgress(tempAlertToEdit.getRange());
                 alertRangeBar.rangeIndicator.setText(""+tempAlertToEdit.getRange());
             }
             if(animate){
                 container.animate().x(0).setDuration(MOTION_DURATION);
             } else {
                 container.setX(0);
             }
         }

         public void hide(boolean animate){
             seekBar.setOnSeekBarChangeListener(null);
             if(animate){
                 container.animate().x(-container.getWidth()).setDuration(MOTION_DURATION);
             } else {
                 container.setX(-container.getWidth());
             }
         }

         private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
                 new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 String rangeInKM = progress+"";
                 if(fromUser){
                     if(tempAlertToEdit!=null){
                         tempAlertToEdit.setRange(progress);
                         rangeIndicator.setText(rangeInKM);
                     }
                 }
             }

             @Override
             public void onStartTrackingTouch(SeekBar seekBar){}
             @Override
             public void onStopTrackingTouch(SeekBar seekBar){}
         };
    }

    private class EditButton{
        private final long MOTION_DURATION = 300;
        View view;
        Button button;

        EditButton(){
            view = getView();
            button = view.findViewById(R.id.buttonEditAlert);
        }

        private void show(boolean animate){
            button.setOnClickListener(onClickListener);
            if(animate){
                button.animate()
                        .x(view.getWidth()-button.getWidth())
                        .setDuration(MOTION_DURATION);
            } else {
                button.setX(view.getWidth()-button.getWidth());
            }
        }

        private void hide(boolean animate){
            button.setOnClickListener(null);
            if(animate){
                button.animate()
                        .x(view.getWidth())
                        .setDuration(MOTION_DURATION);
            } else {
                button.setX(view.getWidth());
            }
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //*** savedInstanceState is set to be null when user presses the
                //    edit button(which starts edit mode), because it stays NOT null
                //    even after fragment restoration, and in order to prevent unwanted
                //    restoration which causes bugs.
                savedInstanceState=null;
                startEditMode(focusedAlert);
            }
        };
    }

    private class ApplyButton{
        private final long MOTION_DURATION = 300;
        private final float GAP_IN_DP = 15;
        private final float GAP_IN_PX = GAP_IN_DP * getContext().getResources()
                .getDisplayMetrics().density;
        View view;
        Button button;

        ApplyButton(){
            view = getView();
            button = view.findViewById(R.id.buttonApply);
        }

        private void show(boolean animate){
            button.setOnClickListener(onClickListener);
            if(animate){
                button.animate()
                        .x(0+GAP_IN_PX);
            } else {
                button.setX(0+GAP_IN_PX);
            }
        }

        private void hide(boolean animate){
            button.setOnClickListener(null);
            if(animate){
                button.animate()
                        .x(-button.getWidth())
                        .setDuration(MOTION_DURATION);
            } else {
                button.setX(-button.getWidth());
            }
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startToEditLocationAndRangeOnly){
                    editorFragment.setAlertData(tempAlertToEdit.getAlertData());
                    finishEditMode(false,false);
                } else {
                    finishEditMode(true,true);
                }
            }
        };

    }

    private class CancelButton{
        private final long MOTION_DURATION = 300;
        private final float GAP_IN_DP = 15;
        private final float GAP_IN_PX = GAP_IN_DP * getContext().getResources()
                .getDisplayMetrics().density;
        View view;
        Button button;

        CancelButton(){
            view = getView();
            button = view.findViewById(R.id.buttonCancel);
        }

        private void show(boolean animate){
            button.setOnClickListener(onClickListener);
            if(animate){
                button.animate().x(view.getWidth()-button.getWidth()-GAP_IN_PX)
                        .setDuration(MOTION_DURATION);
            } else {
                button.setX(view.getWidth()-button.getWidth()-GAP_IN_PX);
            }
        }

        private void hide(boolean animate){
            button.setOnClickListener(null);
            if(animate){
                button.animate().x(view.getWidth()).setDuration(MOTION_DURATION);
            } else {
                button.setX(view.getWidth());
            }
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishEditMode(false,true);
            }
        };

    }
}
