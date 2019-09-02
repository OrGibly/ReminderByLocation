package com.example.mapviewdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.mapviewdemo.Adapters.AlertsListRecycleViewAdapter;
import com.example.mapviewdemo.Data.AlertData;
import com.example.mapviewdemo.Data.DataBaseManager;
import com.example.mapviewdemo.Geofence.GeofencingManager;


public class AlertsListFragment extends Fragment implements Updateable {

    AlertsListRecycleViewAdapter adapter;
    AlertEditorFragment alertEditorFragment = new AlertEditorFragment();
    DataBaseManager.DataSet dataSet = DataBaseManager.getInstance().getDataSet();

    GeofencingManager geofencingManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geofencingManager = new GeofencingManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentContentView = inflater.inflate(R.layout.fragment_alerts_list, container,
                false);
        //RecycleView.
        RecyclerView recyclerView = fragmentContentView.findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true); //for optimization.
        adapter = new AlertsListRecycleViewAdapter(onItemClickListener,onCheckedChangeListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return fragmentContentView;
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            v.setBackgroundColor(getResources().getColor(R.color.colorAlertRVItemPressed));
            PopupMenu menu = new PopupMenu(getContext(),v);
            menu.getMenuInflater().inflate(R.menu.pop_up_menu,menu.getMenu());
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int alertDataId = ((AlertData)v.getTag()).getId();

                    // On Edit click code block.
                    if(item.getItemId()==R.id.edit){
                        alertEditorFragment.bindAlertData(alertDataId,
                                AlertsListFragment.this);
                        getFragmentManager().beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.mainContainer, alertEditorFragment,"AEF")
                                .commit();

                    // On Delete click code block.
                    } else if(item.getItemId()==R.id.delete){
                        AlertData alertData = dataSet.getById(alertDataId);
                        dataSet.remove(alertData);
                        update();
                        Toast.makeText(getContext(),"Alert has been deleted.",
                                Toast.LENGTH_SHORT).show();
                        geofencingManager.unregisterGeofencingRequest(alertData);
                    }
                    return false;
                }
            });
            menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    v.setBackgroundResource(R.drawable.rv_item_background);
                }
            });
            menu.show();
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(!adapter.isOnBind()){
                AlertData alertData = (AlertData)buttonView.getTag();
                alertData.setOn(isChecked);

                // Create/delete a geofence.
                if(alertData.isOn()){
                    geofencingManager.registerGeofencingRequest(alertData);
                } else {
                    geofencingManager.unregisterGeofencingRequest(alertData);
                }
            }
        }
    };

    @Override
    public void update() {
        adapter.notifyDataSetChanged();
    }
}
