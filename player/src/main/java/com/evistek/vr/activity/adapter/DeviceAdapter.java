package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evis on 2016/8/29.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private String mSelectedDevice;

    private List<Device> mDeviceList = new ArrayList<>();

    public DeviceAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mSelectedDevice = getSelectedDevice();

        initDeviceList();
    }

    private void initDeviceList() {
        mDeviceList.add(new Device("小宅Z4", R.drawable.device_xiaozhai_z4));
        mDeviceList.add(new Device("蚁视小檬", R.drawable.device_antvr_xiaomeng));
        mDeviceList.add(new Device("Pico1", R.drawable.device_pico1));
        mDeviceList.add(new Device("通用设备", R.drawable.device_default));
    }

    private String getSelectedDevice() {
        return Utils.getValue(Utils.SHARED_VR_DEVICE, null);
    }

    private void saveSelectedDevice(String deviceName) {
        Utils.saveValue(Utils.SHARED_VR_DEVICE, deviceName);
        E3DApplication.getInstance().getUser().update();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(mLayoutInflater.inflate(R.layout.device_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CardViewHolder cardViewHolder = (CardViewHolder)holder;
        final Device device = mDeviceList.get(position);

        cardViewHolder.mName.setText(device.name);
        cardViewHolder.mImage.setImageResource(device.imageResId);

        if (device.name.equals(mSelectedDevice)) {
            cardViewHolder.mCheck.setImageResource(R.drawable.check);
        } else {
            cardViewHolder.mCheck.setImageResource(R.drawable.uncheck);
        }

        cardViewHolder.mCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedDevice = device.name;
                saveSelectedDevice(mSelectedDevice);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mName;
        public ImageView mCheck;

        public CardViewHolder(View view) {
            super(view);

            mImage = (ImageView) view.findViewById(R.id.device_cardview_image);
            mName = (TextView) view.findViewById(R.id.device_cardview_name);
            mCheck = (ImageView) view.findViewById(R.id.device_cardview_check);
        }
    }

    public class Device {
        public String name;
        public int imageResId;

        public Device(String name, int imageResId) {
            this.name = name;
            this.imageResId = imageResId;
        }
    }
}
