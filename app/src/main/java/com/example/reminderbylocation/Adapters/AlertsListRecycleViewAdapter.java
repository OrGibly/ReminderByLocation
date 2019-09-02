package com.example.reminderbylocation.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.reminderbylocation.Data.AlertData;
import com.example.reminderbylocation.Data.DataBaseManager;
import com.example.mapviewdemo.R;

public class AlertsListRecycleViewAdapter extends RecyclerView.Adapter<AlertsListRecycleViewAdapter.MyViewHolder> {
    //Data accessing and mutating delegate object.
    private DataBaseManager.DataSet dataSet = DataBaseManager.getInstance().getDataSet();
    //RecycleView item's onClick listener.
    private View.OnClickListener onItemClickListener;
    //Item's switch button check changed listener.
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    /**
     * Constructor.
     * @param onItemClickListener RecycleView item's onClick listener.
     * @param onCheckedChangeListener Item's switch button check changed listener.
     */
    public AlertsListRecycleViewAdapter(View.OnClickListener onItemClickListener ,
                                        CompoundButton.OnCheckedChangeListener onCheckedChangeListener){
        this.onItemClickListener = onItemClickListener;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycle_view_item_alert,parent,false);
        return new MyViewHolder(itemView);
    }

    private boolean onBind = false;
    public boolean isOnBind() {
        return onBind;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        onBind = true;
        AlertData alertData = dataSet.get(position);
        holder.itemView.setOnClickListener(onItemClickListener);
        holder.itemView.setTag(dataSet.get(position));
        holder.alertName.setText(alertData.getName());
        holder.alertDetails.setText(alertData.getDetails());
        holder.switchBtn.setChecked(alertData.isOn());
        holder.switchBtn.setTag(dataSet.get(position));
        holder.switchBtn.setOnCheckedChangeListener(onCheckedChangeListener);
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

//------------------------------------ View Holder -------------------------------------
    public class MyViewHolder extends RecyclerView.ViewHolder {
        //Item's text fields.
        TextView alertName, alertDetails;
        Switch switchBtn;

    /**
     * Constructor.
     * @param itemView RecycleView item.
     */
    public MyViewHolder(View itemView) {
            super(itemView);
            alertName = itemView.findViewById(R.id.alertsListTextViewName);
            alertDetails = itemView.findViewById(R.id.alertsListTextViewDetails);
            switchBtn = itemView.findViewById(R.id.alertsListSwitchBtn);
        }
    }
}
