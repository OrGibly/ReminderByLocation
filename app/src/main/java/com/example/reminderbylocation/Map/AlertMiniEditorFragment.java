package com.example.reminderbylocation.Map;

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

import com.example.reminderbylocation.AlertEditorFragment;
import com.example.reminderbylocation.AlertsMapFragment;
import com.example.reminderbylocation.Data.AlertData;
import com.example.mapviewdemo.R;
import com.google.android.gms.maps.model.LatLng;

public class AlertMiniEditorFragment extends Fragment {
    // UI components.
    private TextView nameTitle;
    private TextView detailsTitle;
    private EditText name;
    private EditText details;
    private Button applyBtn;
    private String applyBtnText;
    private static final String APPLY_BUTTON_TEXT_BUNDLE_KEY = "applyButtonTextBundleKey";
    private Button cancelBtn;
    private static final String FRAGMENT_TITLE_BUNDLE_KEY = "fragmentTitleBundleKey";
    private String strFragmentTitle;
    private TextView tvFragmentTitle;

    //
    private static final String BOUND_FRAGMENT_TAG_KEY = "boundFragmentTagKey";
    private AlertsMapFragment boundMapFragment;

    // Creation of a new alert fields.
    private static final String IS_NEW_ALERT_BUNDLE_KEY = "isNewAlertBundleKey";
    private boolean isNewAlert = false;
    private static final String NEW_ALERT_LAT_BUNDLE_KEY = "newAlertLatBundleKey";
    private static final String NEW_ALERT_LON_BUNDLE_KEY = "newAlertLonBundleKey";
    private LatLng newAlertLocation;
    private final static int DEFAULT_RANGE = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            boundMapFragment = (AlertsMapFragment)getFragmentManager().findFragmentByTag(
                    savedInstanceState.getString(BOUND_FRAGMENT_TAG_KEY));
            isNewAlert = savedInstanceState.getBoolean(IS_NEW_ALERT_BUNDLE_KEY);
            if(isNewAlert){
                double lat = savedInstanceState.getDouble(NEW_ALERT_LAT_BUNDLE_KEY);
                double lon = savedInstanceState.getDouble(NEW_ALERT_LON_BUNDLE_KEY);
                newAlertLocation = new LatLng(lat,lon);
            }
            applyBtnText = savedInstanceState.getString(APPLY_BUTTON_TEXT_BUNDLE_KEY);
            strFragmentTitle = savedInstanceState.getString(FRAGMENT_TITLE_BUNDLE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext()).inflate(
                R.layout.fragment_alert_mini_editor,container,false);
        //buttons
        applyBtn = contentView.findViewById(R.id.buttonApplyMiniEditor);
        if(applyBtnText!=null)applyBtn.setText(applyBtnText);
        applyBtn.setOnClickListener(onApplyClickListener);
        cancelBtn = contentView.findViewById(R.id.buttonCancelMiniEditor);
        cancelBtn.setOnClickListener(onCancelClickListener);
        //text fields
        name = contentView.findViewById(R.id.editTextAlertName);
        name.addTextChangedListener(nameTextWatcher);
        nameTitle = contentView.findViewById(R.id.textViewAlertName);
        AlertEditorFragment.EditTextTitleMotion.setTitleMotion(name,nameTitle);
        details = contentView.findViewById(R.id.editTextDetails);
        detailsTitle = contentView.findViewById(R.id.textViewDetails);
        AlertEditorFragment.EditTextTitleMotion.setTitleMotion(details,detailsTitle);
        // set name and details fields.
        if(savedInstanceState==null){
            if(isNewAlert) {
                name.setText("");
                details.setText("");
            } else {
                Alert tempAlertToEdit = boundMapFragment.getTempAlertToEdit();
                name.setText(tempAlertToEdit.getName());
                details.setText(tempAlertToEdit.getDetails());
            }
        } //else it will be restored automatically.

        //set fragment title.
        tvFragmentTitle = contentView.findViewById(R.id.textViewTitleMiniEditor);
        if(this.strFragmentTitle==null){
            tvFragmentTitle.setVisibility(View.GONE);
            tvFragmentTitle.setText("");
        } else {
            tvFragmentTitle.setVisibility(View.VISIBLE);
            tvFragmentTitle.setText(strFragmentTitle);
        }

        //fade-in fragment's view.
        contentView.setAlpha(0f);
        contentView.animate().alpha(1f).setDuration(300);
        return contentView;
    }

    public  void bindAlertsMapFragment(AlertsMapFragment alertsMapFragment){
        this.boundMapFragment = alertsMapFragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BOUND_FRAGMENT_TAG_KEY,boundMapFragment.getTag());
        outState.putBoolean(IS_NEW_ALERT_BUNDLE_KEY, isNewAlert);
        if(isNewAlert){
            outState.putDouble(NEW_ALERT_LAT_BUNDLE_KEY,newAlertLocation.latitude);
            outState.putDouble(NEW_ALERT_LON_BUNDLE_KEY,newAlertLocation.longitude);
        }
        outState.putString(APPLY_BUTTON_TEXT_BUNDLE_KEY,applyBtnText);
        outState.putString(FRAGMENT_TITLE_BUNDLE_KEY,strFragmentTitle);
    }

    private void closeKeyboard(){
        View view = getView();
        InputMethodManager imm = (InputMethodManager)getActivity().
                getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void createNewAlert(LatLng location){
        isNewAlert = true;
        newAlertLocation = location;
        strFragmentTitle = "New Alert";
        applyBtnText = "Create";
    }

    private View.OnClickListener onApplyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isNewAlert){
                AlertData alertData = new AlertData(name.getText().toString(),newAlertLocation,
                        DEFAULT_RANGE,details.getText().toString(),true);
                boundMapFragment.startEditModeWithNewAlert(alertData);
            } else {
                Alert tempAlertToEdit = boundMapFragment.getTempAlertToEdit();
                tempAlertToEdit.setName(name.getText().toString());
                tempAlertToEdit.setDetails(details.getText().toString());
                tempAlertToEdit.refreshInfoWindow();
            }
            getFragmentManager().beginTransaction().remove(AlertMiniEditorFragment.this).commit();
            closeKeyboard();
        }
    };

    View.OnClickListener onCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager().beginTransaction().remove(AlertMiniEditorFragment.this).commit();
            closeKeyboard();
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
}
