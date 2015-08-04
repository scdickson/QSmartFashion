package com.qualcomm.qsmartfashion.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.fragments.DeviceFragment;

/**
 * Created by sdickson on 8/3/15.
 */
public class DeviceListAdapter extends BaseAdapter
{
    Context context;

    public DeviceListAdapter(Context context)
    {
        this.context = context;
    }

    public int getCount()
    {
        return DeviceFragment.devices.size();
    }

    public Object getItem(int position)
    {
        Object devicesArray[] = DeviceFragment.devices.keySet().toArray();
        return devicesArray[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.device_list_item, parent, false);

        TextView name = (TextView) itemView.findViewById(R.id.device_name);
        TextView uuid = (TextView) itemView.findViewById(R.id.device_uuid);
        ImageView rssi = (ImageView) itemView.findViewById(R.id.device_rssi);
        ImageView type = (ImageView) itemView.findViewById(R.id.device_image);

        BluetoothDevice device = (BluetoothDevice) getItem(position);
        Integer device_rssi = (Integer) DeviceFragment.devices.get(device);

        if(device.getName() != null && !device.getName().isEmpty())
        {
            name.setText(device.getName());
            uuid.setText(device.getAddress());
        }
        else
        {
            name.setText(device.getAddress());
        }

        if(device_rssi >= -20) //4
        {
            rssi.setImageDrawable(context.getDrawable(R.drawable.signal_4));
        }
        else if(device_rssi < -20 && device_rssi >= -40) //3
        {
            rssi.setImageDrawable(context.getDrawable(R.drawable.signal_3));
        }
        else if(device_rssi < -40 && device_rssi >= -60) //2
        {
            rssi.setImageDrawable(context.getDrawable(R.drawable.signal_2));
        }
        else if(device_rssi < -60 && device_rssi >= -80) //1
        {
            rssi.setImageDrawable(context.getDrawable(R.drawable.signal_1));
        }
        else if(device_rssi < -80) //0
        {
            rssi.setImageDrawable(context.getDrawable(R.drawable.signal_0));
        }

        return itemView;
    }
}
