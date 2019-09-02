package com.example.mapviewdemo.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.example.mapviewdemo.App;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * A manager for the interaction with the SQLite database.
 * * Singleton pattern.
 */
public class DataBaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alerts.db";
    private static final String TABLE_NAME = "alerts";
    private static final String COL_1 = "id";
    private static final String COL_2 = "name";
    private static final String COL_3 = "details";
    private static final String COL_4 = "lat";
    private static final String COL_5 = "lon";
    private static final String COL_6 = "range";
    private static final String COL_7 = "isOn";

    // Represents not in use id (for items the are not in database).
    public static final int NULL_ID = -1;
    private DataSet dataSet;

    // Singleton instance.
    private static DataBaseManager instance;
    /**
     * Provides a singleton instance.
     * @return DataBaseManager instance.
     */
    public static DataBaseManager getInstance(){
        if(instance==null){
            instance = new DataBaseManager(App.getAppContext());
        }
        return instance;
    }

    /**
     * Constructor.
     * @param context The context used in this instance.
     */
    private DataBaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        dataSet = new DataSet();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+"("+
                        COL_1+" integer primary key autoincrement ,"+
                        COL_2+" text,"+
                        COL_3+" text,"+
                        COL_4+" real,"+
                        COL_5+" real,"+
                        COL_6+" integer,"+
                        COL_7+" boolean"+");"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * DataSet getter.
     * @return The related DataSet of the data that managed by the DataBaseManager.
     */
    public DataSet getDataSet(){
        return dataSet;
    }

    /**
     * Adds the given AlertData to the database.
     * @param alertData The AlertData to add.
     * @return The id that was given to the alert by the database.
     */
    private int add(AlertData alertData){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME + "("+
                COL_2+","+
                COL_3+","+
                COL_4+","+
                COL_5+","+
                COL_6+","+
                COL_7+")"+
                " Values ("+
                "'"+alertData.getName()+"'"+","+
                "'"+alertData.getDetails()+"'"+","+
                alertData.getLocation().latitude+","+
                alertData.getLocation().longitude+","+
                alertData.getRange()+","+
                (alertData.isOn()?1:0)+
                ");"
        );
        Cursor cursor = db.rawQuery("select max(id) from "+TABLE_NAME+";",null);
        int id = NULL_ID;
        if(cursor.moveToFirst())id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    /**
     * Makes a List from the alerts' data in database.
     * @return A List of AlertData.
     */
    private List<AlertData> getDataList(){
        ArrayList<AlertData> arrayList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+TABLE_NAME+";",null);
        if(cursor.moveToNext()){
            do{
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String details = cursor.getString(2);
                LatLng location = new LatLng(cursor.getDouble(3),cursor.getDouble(4));
                int range = cursor.getInt(5);
                boolean isOn = cursor.getInt(6)==1;
                AlertData alertData =  new AlertData(name,location,range,details,isOn);
                alertData.setId(id);
                alertData.bindDBManager(this);
                arrayList.add(alertData);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return arrayList;
    }

    /**
     * Removes an alert's data from the database.
     * @param alertId The id of the alert to remove.
     */
    private void remove(int alertId){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+TABLE_NAME+" where "+COL_1+" = "+alertId+";");
    }

    /**
     * Updates the name field of an alert's data in database.
     * @param alertData The AlertData that represents the data to update.
     * @param newName The name to set.
     */
    void updateName(AlertData alertData, String newName){
        getWritableDatabase().execSQL("update "+TABLE_NAME+" set "+
                COL_2+" = '"+newName+
                "' where "+COL_1+" = "+alertData.getId()+";");
    }


    /**
     * Updates the details field of an alert's data in database.
     * @param alertData The AlertData that represents the data to update.
     * @param newDetails The details to set.
     */
    void updateDetails(AlertData alertData, String newDetails){
        getWritableDatabase().execSQL("update "+TABLE_NAME+" set "+
                COL_3+" = '"+newDetails+
                "' where "+COL_1+" = "+alertData.getId()+";");
    }

    /**
     * Updates the location field of an alert's data in database.
     * @param alertData The AlertData that represents the data to update.
     * @param newLocation The location to set.
     */
    void updateLocation(AlertData alertData, LatLng newLocation) {
        getWritableDatabase().execSQL("update "+TABLE_NAME+" set "+
                COL_4+" = "+newLocation.latitude+", "+
                COL_5+" = "+newLocation.longitude+
                " where "+COL_1+" = "+alertData.getId()+";");
    }

    /**
     * Updates the location field of an alert's data in database.
     * @param alertData The AlertData that represents the data to update.
     * @param newRange The range to set.
     */
    void updateRange(AlertData alertData, int newRange){
        getWritableDatabase().execSQL("update "+TABLE_NAME+" set "+
                COL_6+" = "+newRange+
                " where "+COL_1+" = "+alertData.getId()+";");
    }

    /**
     * Updates the isOn field of an alert's data in database.
     * @param alertData The AlertData that represents the data to update.
     * @param on The isOn value to set.
     */
    void updateIsOn(AlertData alertData, boolean on){
        getWritableDatabase().execSQL("update "+TABLE_NAME+" set "+
                COL_7+" = "+(on? 1:0)+
                " where "+COL_1+" = "+alertData.getId()+";");
    }

    /**
     * A delegate of the dataBaseManager that provides accessibility to the data.
     * * Should be always synchronized with the data in the DB.
     */
    public class DataSet{
        private List<AlertData> alerts;

        /**
         * Constructor.
         */
        DataSet(){
            alerts = getDataList();
        }

        /**
         * Adds an AlertData to the DataSet and its data to the related database
         * if it is not already contains it.
         * @param alertData The AlertData to add.
         */
        public void add(AlertData alertData) {
            if(!alerts.contains(alertData)){
                int id = DataBaseManager.this.add(alertData);
                alertData.setId(id);
                alertData.bindDBManager(DataBaseManager.this);
                alerts.add(alertData);
            }
        }

        /**
         * Removes an AlertData from the DataSet and its data from the related database.
         * @param alertData The alertData to remove.
         */
        public void remove(AlertData alertData) {
            DataBaseManager.this.remove(alertData.getId());
            alerts.remove(alertData);
            alertData.bindDBManager(null);
        }

        /**
         * Returns the AlertData at the specified position in the DataSet.
         * @param i The position of the wanted AlertData.
         * @return An AlertData from the DataSet.
         */
        public AlertData get(int i){
            return alerts.get(i);
        }

        /**
         * Returns the AlertData with the specified id from the DataSet.
         * @param id The id of the wanted AlertData.
         * @return An AlertData from the DataSet if exist, else null.
         */
        public AlertData getById(int id){
            for (AlertData alertData:alerts){
                if(alertData.getId()==id)return alertData;
            }
            return null;
        }

        /**
         * Returns the number of items in the DataSet.
         * @return DataSet's size.
         */
        public int size(){
            return alerts.size();
        }

    }
}
