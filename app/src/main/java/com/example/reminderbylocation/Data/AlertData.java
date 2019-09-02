package com.example.reminderbylocation.Data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;



/**
 * This class communicates with the database (if was bound) through the boundDBManager
 * (DataBaseManager instance).
 */
// Any change in this class's data is conditional to change in data base,
// represented by boundDBManager.
public class AlertData {

    private DataBaseManager boundDBManager;
    // This Default value Indicates that the current alertData isn't in database.
    // The id should be provided only by the DataBaseManager when added and bounded.
    private int id = DataBaseManager.NULL_ID;

    // Alert's data fields:
    private String name;
    private String details;
    private LatLng location;
    //Range in KM.
    private int range;
    private boolean isOn;

    /**
     * Constructor.
     * @param name The name of the alert.
     * @param location The location of the alert.
     * @param range The range of the alert.
     * @param details The details of the alert.
     * @param isON Indicates if the alert should be On or Off.
     */
    public AlertData(String name, LatLng location, int range, String details, boolean isON) {
        this.name = name;
        this.location = location;
        this.range = range;
        this.details = details;
        this.isOn = isON;
    }

    /**
     * Copy Constructor (without id and bound DataBaseManager).
     * @param from The AlertData to copy from.
     */
    public AlertData(AlertData from){
        copyFrom(from);
    }

    /**
     * Binds current AlertData to DataBaseManager.
     * @param dataBaseManager The DataBaseManager to bind to.
     */
    void bindDBManager(@Nullable  DataBaseManager dataBaseManager) {
        boundDBManager = dataBaseManager;
    }

    /**
     * Id getter.
     * @return Id.
     */
    public int getId() {
        return id;
    }

    /**
     * Id setter.
     * @param id Id to set.
     */
    void setId(int id) {
        this.id = id;
    }

    /**
     * Name getter.
     * @return Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Name setter.
     * @param name Name to set.
     */
    public void setName(String name){
        if(boundDBManager!=null){
            boundDBManager.updateName(this,name);
        }
        this.name = name;
    }

    /**
     * Location getter.
     * @return Location.
     */
    public LatLng getLocation() {
        return location;
    }

    /**
     * Location setter.
     * @param location Location to set.
     */
    public void setLocation(LatLng location) {
        if(boundDBManager!=null){
            boundDBManager.updateLocation(this,location);
        }
        this.location = location;
    }

    /**
     * Range getter.
     * @return Range.
     */
    public int getRange() {
        return range;
    }

    /**
     * Range setter.
     * @param range Range to set.
     */
    public void setRange(int range) {
        if(boundDBManager!=null){
            boundDBManager.updateRange(this,range);
        }
        this.range = range;
    }

    /**
     * Range getter.
     * @return Range.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Details setter.
     * @param details Details to set.
     */
    public void setDetails(String details) {
        if(boundDBManager!=null){
            boundDBManager.updateDetails(this,details);
        }
        this.details = details;
    }

    /**
     * On/Off getter.
     * @return True if On.
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * On/Off setter.
     * @param On isOn to set.
     */
    public void setOn(boolean On) {
        if(boundDBManager!=null){
            boundDBManager.updateIsOn(this, On);
        }
        this.isOn = On;
    }

    /**
     * copy method.
     * @param from The AlertData to copy from.
     */
    private void copyFrom(AlertData from){
        // All arguments are immutable, so deep copy isn't necessary here.
        if(boundDBManager==null){
            this.name = from.getName();
            this.details = from.getDetails();
            this.location= from.getLocation();
            this.range = from.getRange();
            this.isOn = from.isOn();
        } else {
            setName(from.getName());
            setDetails(from.getDetails());
            setLocation(from.getLocation());
            setRange(from.getRange());
            setOn(from.isOn);
            this.location= from.getLocation();
            this.range = from.getRange();
            this.name = from.getName();
            this.details = from.getDetails();
            this.isOn = from.isOn();
            }
        }
}
