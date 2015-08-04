package com.qualcomm.qsmartfashion.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.qualcomm.qsmartfashion.Constants;
import com.qualcomm.qsmartfashion.MetricsActivity;
import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.adapters.DeviceListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sdickson on 8/3/15.
 */
public class DeviceFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "DeviceFragment";
    public static final int REQUEST_ENABLE_BT = 1111;
    public static HashMap<BluetoothDevice, Integer> devices = new HashMap<BluetoothDevice, Integer>();
    public static boolean isScanning = false;
    public static int num_devices = 0;

    View rootView;
    ListView deviceList;
    TextView noDevices;
    DeviceListAdapter adapter;
    BluetoothAdapter btAdapter;
    BluetoothManager btManager;

    public static DeviceFragment newInstance(int sectionNumber)
    {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MetricsActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.device_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
                if(isScanning)
                {
                    item.setTitle("Scan");
                    btAdapter.stopLeScan(mLeScanCallback);
                    isScanning = false;
                }
                else
                {
                    num_devices = 0;
                    devices.clear();
                    adapter.notifyDataSetChanged();
                    noDevices.setText("Scanning...");
                    noDevices.setVisibility(View.VISIBLE);
                    deviceList.setVisibility(View.GONE);
                    ((MetricsActivity) getActivity()).getSupportActionBar().setTitle("Devices");

                    item.setTitle("Stop");
                    btAdapter.startLeScan(mLeScanCallback);
                    isScanning = true;
                }

                break;
        }

        return false;
    }

    public DeviceFragment()
    {
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
                {
                    if(device.getName() != null)
                    {
                        devices.put(device, rssi);
                        adapter.notifyDataSetChanged();
                        ((MetricsActivity) getActivity()).getSupportActionBar().setTitle("Devices (" + (++num_devices) + ")");
                        if(noDevices.getVisibility() == View.VISIBLE)
                        {
                            noDevices.setVisibility(View.GONE);
                            deviceList.setVisibility(View.VISIBLE);
                        }
                    }
                }
            };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("qsf", "Connect OK");
            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {

                Log.d("qsf", "Discovering Services...");
                gatt.discoverServices();

            } else if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.d("qsf", "DISCONNECTED");

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            BluetoothGattService service = gatt.getService(Constants.QSF_SERVICE);
            BluetoothGattCharacteristic rxCharc = service.getCharacteristic(Constants.QSF_DEVICE_RX_UUID);
            BluetoothGattCharacteristic txCharc = service.getCharacteristic(Constants.QSF_DEVICE_TX_UUID);

            gatt.setCharacteristicNotification(rxCharc, true);
            BluetoothGattDescriptor clientConfig = rxCharc.getDescriptor(Constants.QSF_DEVICE_DESCRIPTOR);
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(clientConfig);

            //String data = "Z PLEASE FUCKING WORK";
            //txCharc.setValue(data.getBytes());
            //gatt.writeCharacteristic(txCharc);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("qsf", "onCharacteristicRead ( characteristic :"
                        + characteristic + " ,status, : " + status + ")");
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d("qsf", characteristic.getStringValue(0));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("qsf", "onCharacteristicWrite ( characteristic :"
                        + characteristic + " ,status : " + status + ")");
            }
        };

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor device, int status) {
            Log.d("qsf", "onDescriptorRead (device : " + device + " , status :  "
                    + status + ")");
            super.onDescriptorRead(gatt, device, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor arg0, int status) {
            Log.d("qsf", "onDescriptorWrite (arg0 : " + arg0 + " , status :  "
                    + status + ")");
            super.onDescriptorWrite(gatt, arg0, status);
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.device_fragment_layout, container, false);
        setHasOptionsMenu(true);
        deviceList = (ListView) rootView.findViewById(R.id.device_list);
        noDevices = (TextView) rootView.findViewById(R.id.device_no_devices);
        adapter = new DeviceListAdapter(getActivity());
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(isScanning)
                {
                    btAdapter.stopLeScan(mLeScanCallback);
                }
                Object devicesArray[] = DeviceFragment.devices.keySet().toArray();
                BluetoothDevice selected = (BluetoothDevice) devicesArray[position];
                selected.connectGatt(getActivity().getApplicationContext(), true, mGattCallback);
            }
        });
        deviceList.setAdapter(adapter);

        btManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        return rootView;
    }
}
