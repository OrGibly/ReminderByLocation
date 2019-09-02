package com.example.mapviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapviewdemo.Data.AlertData;
import com.example.mapviewdemo.Data.DataBaseManager;
import com.google.android.gms.maps.model.LatLng;

public class AlertEditorFragment extends Fragment{

    //Used to save and restore states by Bundle.
    private static final String BOUND_ALERT_DATA_ID_KEY = "BADIK";
    //Used to get back from the FragmentManager the fragment that should be
    //updated(AlertsListFragment) when the editor page applies any changes.
    private static final String FRAGMENT_TO_UPDATE_TAG_KEY = "FTUTK";

    //Alert List related fields.
    private int boundAlertDataId;
    private AlertData boundAlertData;

    private Updateable fragmentToUpdate;

    private String name;
    private String details;
    private LatLng location;
    private int range;

    private EditText alertName, alertDetails;
    private TextView alertNameTitle, alertDetailsTitle,
            alertLocation, alertRange;
    private final static String LOCATION_BUNDLE_KEY = "LBK";
    private final static String RANGE_BUNDLE_KEY = "RBK";
    private Button applyBtn, cancelBtn, locationAndRangeBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            boundAlertDataId = savedInstanceState.getInt(BOUND_ALERT_DATA_ID_KEY);
            Fragment fragment = getFragmentManager().findFragmentByTag(
                    savedInstanceState.getString(FRAGMENT_TO_UPDATE_TAG_KEY));
            fragmentToUpdate=(Updateable)fragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_alert_editor, container, false);
        //buttons
        locationAndRangeBtn = contentView.findViewById(R.id.buttonLocationRange);
        locationAndRangeBtn.setOnClickListener(onLocationAndRangeClickListener);
        applyBtn = contentView.findViewById(R.id.buttonApplyAlertEditor);
        applyBtn.setOnClickListener(onApplyClickListener);
        cancelBtn = contentView.findViewById(R.id.buttonCancelAlertEditor);
        cancelBtn.setOnClickListener(onCancelClickListener);
        //text fields
        alertName = contentView.findViewById(R.id.editTextAlertName);
        alertName.addTextChangedListener(nameTextWatcher);
        alertNameTitle = contentView.findViewById(R.id.textViewAlertName);
        EditTextTitleMotion.setTitleMotion(alertName, alertNameTitle);

        alertDetails = contentView.findViewById(R.id.editTextDetails);
        alertDetailsTitle = contentView.findViewById(R.id.textViewDetails);
        EditTextTitleMotion.setTitleMotion(alertDetails, alertDetailsTitle);

        alertLocation = contentView.findViewById(R.id.textViewLocation);
        alertRange = contentView.findViewById(R.id.textViewRange);

        boundAlertData = DataBaseManager.getInstance().getDataSet().getById(boundAlertDataId);
        if(savedInstanceState!=null){
            alertLocation.setText(savedInstanceState.getString(LOCATION_BUNDLE_KEY));
            alertRange.setText(savedInstanceState.getString(RANGE_BUNDLE_KEY));
        }

        contentView.setAlpha(0f);
        contentView.animate().alpha(1f).setDuration(300);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAlertData(boundAlertData);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(BOUND_ALERT_DATA_ID_KEY,boundAlertDataId);
        outState.putString(FRAGMENT_TO_UPDATE_TAG_KEY,((Fragment)fragmentToUpdate).getTag());
        outState.putString(LOCATION_BUNDLE_KEY,alertLocation.getText().toString());
        outState.putString(RANGE_BUNDLE_KEY,alertRange.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public void bindAlertData(int alertDataId, Updateable FragmentToUpdate) {
        this.boundAlertDataId = alertDataId;
        this.fragmentToUpdate = FragmentToUpdate;
    }

    public void setAlertData(AlertData alertData) {
        if (alertData != null) {
            this.name = alertData.getName();
            alertName.setText(this.name);
            this.location = alertData.getLocation();
            alertLocation.setText(
                    "lat: " + location.latitude + ", lon: " + location.longitude);
            this.range = alertData.getRange();
            alertRange.setText(range + " km");
            this.details = alertData.getDetails();
            alertDetails.setText(this.details);
        } else {
            Toast.makeText(getActivity(), "null data object.", Toast.LENGTH_SHORT).show();
            alertName.setText("null");
            alertName.setEnabled(false);
            alertLocation.setText("null");
            alertRange.setText("null");
            alertDetails.setText("null");
            alertDetails.setEnabled(false);
            applyBtn.setEnabled(false);
        }
    }

    private View.OnClickListener onApplyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertData alertData = boundAlertData;
            alertData.setName(alertName.getText().toString());
            alertData.setDetails(alertDetails.getText().toString());
            alertData.setLocation(location);
            alertData.setRange(range);
            fragmentToUpdate.update();
            getFragmentManager().beginTransaction().remove(AlertEditorFragment.this).commit();
            closeKeyboard();
        }
    };

    private View.OnClickListener onCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager().beginTransaction().remove(AlertEditorFragment.this).commit();
            closeKeyboard();
        }
    };

    private void closeKeyboard(){
        View view = cancelBtn;
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private View.OnClickListener onLocationAndRangeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeKeyboard();
            AlertsMapFragment alertsMapFragment = new AlertsMapFragment();
            alertsMapFragment.startToEditLocationAndRangeOnly(boundAlertDataId, AlertEditorFragment.this);
            getFragmentManager().beginTransaction()
                    .add(R.id.mainContainer,alertsMapFragment,"AMF")
                    .addToBackStack(null)
                    .commit();
        }
    };

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length()==0)applyBtn.setEnabled(false);
            else if (!applyBtn.isEnabled())applyBtn.setEnabled(true);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    public static class EditTextTitleMotion {
        private static final float MOTION_RANGE = 200; //px
        private static final int MOTION_DURATION = 100; //milli seconds

        public static void setTitleMotion(final EditText input, final TextView title) {
            final float titleInitialX = title.getX();

            if(input.getText().length()==0){ //if there is a Hint.
                // disappear title.
                title.setX(titleInitialX + MOTION_RANGE);
                title.setAlpha(0f);
                title.setVisibility(View.GONE);
            }

            input.addTextChangedListener(new TextWatcher() {
                int textLength;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    textLength = s.length();

                    if(textLength==0){
                        // disappear title.
                        title.animate()
                                .alpha(0f)
                                .x(titleInitialX + MOTION_RANGE)
                                .setDuration(MOTION_DURATION);
                        title.postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if(textLength==0) {
                                                      title.setVisibility(View.GONE);
                                                  }
                                              }}
                                ,MOTION_DURATION);
                    } else {
                        // appear title.
                        title.post(new Runnable() {
                            @Override
                            public void run() {
                                title.setVisibility(View.VISIBLE);
                            }
                        });
                        title.animate()
                                .alpha(1f)
                                .x(titleInitialX)
                                .setDuration(MOTION_DURATION);
                    }
                }
            });
        }

    }
}
