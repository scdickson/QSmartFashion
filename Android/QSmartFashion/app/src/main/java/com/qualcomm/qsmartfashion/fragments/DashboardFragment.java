package com.qualcomm.qsmartfashion.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.qualcomm.qsmartfashion.Constants;
import com.qualcomm.qsmartfashion.MetricsActivity;
import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.utils.QSFLocationListener;
import com.qualcomm.qsmartfashion.utils.UnitConverter;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sdickson on 8/3/15.
 */
public class DashboardFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "DashboardFragment";
    public static final String locationProvider = LocationManager.NETWORK_PROVIDER;
    public enum HEALTH_STATUS {HEALTHY, AT_RISK, UNHEALTHY};

    WebView heartHistory, tempHistory;
    EditText simulatedHeart;
    EditText simulatedTemp;
    View rootView;
    LocationManager locationManager;
    QSFLocationListener locationListener;
    StringBuilder sb = new StringBuilder();
    TextView heartBpm, tempF, heartTrend, tempTrend, healthStatus;
    ImageView healthStatusImg;
    LinearLayout healthStatusLayout;

    public static double last_hr_meas = -1;
    public static double last_temp_meas = -1;
    ProgressDialog btWait;

    public int numMeasurements = 1;

   ArrayList<Object[]> heartMeasurements = new ArrayList<Object[]>();
    ArrayList<Object[]> tempMeasurements = new ArrayList<Object[]>();

    public Handler locationFoundHandler = new Handler()
    {
            public void handleMessage(Message msg)
            {
                locationManager.removeUpdates(locationListener);
                locationListener = null;
                final Bundle data = msg.getData();
                ParseObject locObject = new ParseObject("DataMeasurement");
                locObject.put("heartrate", data.getDouble("heartrate"));
                locObject.put("temperature", data.getDouble("temperature"));
                locObject.put("user", ParseUser.getCurrentUser());
                locObject.put("lat", data.getDouble("lat"));
                locObject.put("lng", data.getDouble("lng"));
                locObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e)
                    {
                        if (e == null)
                        {
                            //Toast.makeText(getActivity(), "Measurement Saved", Toast.LENGTH_SHORT).show();
                            //addMeasurement(data.getDouble("heartrate"), data.getDouble("temperature"));
                            loadMetricsFromParse();
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Error Saving Measurement", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
    };

    public static DashboardFragment newInstance(int sectionNumber)
    {
        DashboardFragment fragment = new DashboardFragment();
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
        inflater.inflate(R.menu.dashboard_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_simulate_data:
                takeSimulatedDataPoint();
                break;
        }

        return false;
    }

    public void loadMetricsFromParse()
    {
        heartMeasurements.clear();
        tempMeasurements.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DataMeasurement");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.setLimit(15);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> datapointList, ParseException e) {
                if (e == null) {
                    Collections.reverse(datapointList);
                    for (int i = 0; i < datapointList.size(); i++) {
                        ParseObject obj = datapointList.get(i);
                        heartMeasurements.add(new Object[]{obj.getCreatedAt(), obj.getDouble("heartrate")});
                        tempMeasurements.add(new Object[]{obj.getCreatedAt(), UnitConverter.cToF(obj.getDouble("temperature"))});
                    }

                    ParseObject last = datapointList.get(datapointList.size() - 1);
                    last_hr_meas = last.getDouble("heartrate");
                    heartBpm.setText(last_hr_meas + " bpm");
                    last_temp_meas = UnitConverter.cToF(last.getDouble("temperature"));
                    NumberFormat formatter = new DecimalFormat("#0.0");
                    tempF.setText(formatter.format(last_temp_meas) + "° F");

                    switch (getCurrentHealthStatus()) {
                        case HEALTHY:
                            healthStatus.setText("You are healthy!");
                            healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_happy));
                            healthStatusLayout.setBackgroundColor(Color.parseColor("#89a647"));
                            break;
                        case AT_RISK:
                            healthStatus.setText("You are at risk!");
                            healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_meh));
                            healthStatusLayout.setBackgroundColor(Color.parseColor("#e5a13a"));
                            break;
                        case UNHEALTHY:
                            healthStatus.setText("You are unhealthy!");
                            healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_unhappy));
                            healthStatusLayout.setBackgroundColor(Color.parseColor("#e93b46"));
                            break;
                    }


                    if (datapointList.size() > 2) {
                        ParseObject penultimate = datapointList.get(datapointList.size() - 2);
                        double penultimate_hr_meas = penultimate.getDouble("heartrate");
                        double penultimate_temp_meas = penultimate.getDouble("temperature");

                        if (last_hr_meas - penultimate_hr_meas > 0) {
                            heartTrend.setText("⬆");
                        } else if (last_hr_meas - penultimate_hr_meas < 0) {
                            heartTrend.setText("⬇");
                        } else {
                            heartTrend.setText("");
                        }

                        if (last_temp_meas - penultimate_temp_meas > 0) {
                            tempTrend.setText("⬆");
                        } else if (last_temp_meas - penultimate_temp_meas < 0) {
                            tempTrend.setText("⬇");
                        } else {
                            tempTrend.setText("");
                        }
                    }

                    updateGraphs();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public HEALTH_STATUS getCurrentHealthStatus()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        Date now = Calendar.getInstance().getTime();
        long diff = Math.abs(now.getTime() - currentUser.getDate("birthdate").getTime());
        long age = diff / 1000 / 60 / 60 / 24 / 365;

        double max_heartrate = 0;
        if(currentUser.getString("sex").equals("M"))
        {
            max_heartrate = 190.2 / (1 + Math.exp(0.0453 * (age - 107.5)));
        }
        else
        {
            max_heartrate = 203.7 / (1 + Math.exp(0.033 * (age - 104.3)));
        }

        double min_heartrate = max_heartrate * 0.35;

        double last_temp_meas_c = UnitConverter.fToC(last_temp_meas);

        if(last_temp_meas_c <= (36.5 - (36.5*0.1)) || last_temp_meas_c >= (37.5 + (37.5*0.1)) || last_hr_meas <= (min_heartrate - (min_heartrate*0.1)) || last_hr_meas >= (max_heartrate + (max_heartrate*0.1)))
        {
            return HEALTH_STATUS.UNHEALTHY;
        }
        if((last_temp_meas_c < 36.5 && last_temp_meas_c > (36.5 - (36.5*0.1))) || (last_temp_meas_c > 37.5 && last_temp_meas_c < (37.5 + (37.5*0.1))) || (last_hr_meas < min_heartrate && last_hr_meas > (min_heartrate - (min_heartrate*0.1))) || (last_hr_meas > max_heartrate && last_hr_meas < (max_heartrate + (max_heartrate*0.1))))
        {
            return HEALTH_STATUS.AT_RISK;
        }

        return HEALTH_STATUS.HEALTHY;
    }

    public void updateGraphs()
    {
        final Date now = Calendar.getInstance().getTime();

        heartHistory.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                sb.append("var hrData = {labels : [");
                int i = 0;
                for (Object obj[] : heartMeasurements) {
                    Date date = (Date) obj[0];
                    long diff = Math.abs(now.getTime() - date.getTime());
                    long diffSecs = diff / 1000;
                    long diffMins = diff / 1000 / 60;
                    long diffHours = diffMins / 60;

                    if (diffMins < 1) {
                        sb.append("\"" + diffSecs + " s\"");
                        continue;
                    }

                    if (diffHours < 2) {
                        sb.append("\"" + diffMins + " m\"");
                    } else {
                        sb.append("\"" + diffHours + " h\"");
                    }

                    if (i++ < heartMeasurements.size() - 1) {
                        sb.append(",");
                    }
                }

                sb.append("], datasets : [{fillColor : \"rgba(233,59,70,0.4)\",strokeColor : \"#E2AB47\",pointColor : \"#fff\",pointStrokeColor : \"#E2AB47\",data : [");

                i = 0;
                for (Object obj[] : heartMeasurements) {
                    sb.append((Double) obj[1]);
                    if (i++ < heartMeasurements.size() - 1) {
                        sb.append(",");
                    }
                }

                sb.append("]}]}; var hr = document.getElementById('chart').getContext('2d'); new Chart(hr).Line(hrData);");
                view.loadUrl("javascript:" + sb.toString().replace("\n", ""));
            }
        });

        tempHistory.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                sb.append("var tempData = {labels : [");

                int i = 0;
                for (Object obj[] : tempMeasurements) {
                    Date date = (Date) obj[0];
                    long diff = Math.abs(now.getTime() - date.getTime());
                    long diffSecs = diff / 1000;
                    long diffMins = diff / 1000 / 60;
                    long diffHours = diffMins / 60;

                    if (diffMins < 1) {
                        sb.append("\"" + diffSecs + " s\"");
                        continue;
                    }

                    if (diffHours < 2) {
                        sb.append("\"" + diffMins + " m\"");
                    } else {
                        sb.append("\"" + diffHours + " h\"");
                    }

                    if (i++ < tempMeasurements.size() - 1) {
                        sb.append(",");
                    }
                }

                sb.append("], datasets : [{fillColor : \"rgba(17,178,178,0.4)\",strokeColor : \"#11b2b2\",pointColor : \"#fff\",pointStrokeColor : \"#11b2b2\",data : [");

                i = 0;
                for (Object obj[] : tempMeasurements) {
                    sb.append((Double) obj[1]);
                    if (i++ < tempMeasurements.size() - 1) {
                        sb.append(",");
                    }
                }

                sb.append("]}]}; var temp = document.getElementById('chart').getContext('2d'); new Chart(temp).Line(tempData);");
                view.loadUrl("javascript:" + sb.toString().replace("\n", ""));
            }
        });

        heartHistory.loadUrl("file:///android_asset/cv.html");
        tempHistory.loadUrl("file:///android_asset/cv.html");
    }

    public void addMeasurement(double heartrate, double temperature)
    {
        if(last_hr_meas != -1 && last_temp_meas != -1)
        {
            if (heartrate - last_hr_meas > 0) {
                heartTrend.setText("⬆");
            } else if (heartrate - last_hr_meas < 0) {
                heartTrend.setText("⬇");
            } else {
                heartTrend.setText("");
            }

            if (temperature - last_temp_meas > 0) {
                tempTrend.setText("⬆");
            } else if (temperature - last_temp_meas < 0) {
                tempTrend.setText("⬇");
            } else {
                tempTrend.setText("");
            }
        }

        last_hr_meas = heartrate;
        last_temp_meas = UnitConverter.cToF(temperature);
        //tempMeasurements.put(Calendar.getInstance().getTime(), temperature);
        //heartMeasurements.put(Calendar.getInstance().getTime(), heartrate);

        heartBpm.setText(last_hr_meas + " bpm");
        NumberFormat formatter = new DecimalFormat("#0.0");
        tempF.setText(formatter.format(last_temp_meas) + "° F");

        switch(getCurrentHealthStatus())
        {
            case HEALTHY:
                healthStatus.setText("You are healthy!");
                healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_happy));
                healthStatusLayout.setBackgroundColor(Color.parseColor("#89a647"));
                break;
            case AT_RISK:
                healthStatus.setText("You are at risk!");
                healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_meh));
                healthStatusLayout.setBackgroundColor(Color.parseColor("#e5a13a"));
                break;
            case UNHEALTHY:
                healthStatus.setText("You are unhealthy!");
                healthStatusImg.setImageDrawable(getActivity().getDrawable(R.drawable.meter_unhappy));
                healthStatusLayout.setBackgroundColor(Color.parseColor("#e93b46"));
                break;
        }

    }

    protected void takeSimulatedDataPoint() {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.simulated_dp_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Take Simulated Reading");

        simulatedHeart = (EditText) promptView.findViewById(R.id.simulated_dp_heart);
        simulatedTemp = (EditText) promptView.findViewById(R.id.simulated_dp_temp);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        locationListener = new QSFLocationListener(locationFoundHandler, Double.parseDouble(simulatedHeart.getText().toString()), Double.parseDouble(simulatedTemp.getText().toString()));
                        locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
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
            //super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getValue().length <= 8) { //sanity check for garbage data we get sometimes
                String tmp = "";
                for (int i = 0; i < characteristic.getValue().length; i++) { //dumb ASCII characters wtf
                    if (characteristic.getValue()[i] > 32) {
                        tmp += String.valueOf((char) characteristic.getValue()[i]);
                    }
                }

                try
                {
                    final String data[] = tmp.split(":");
                    if (data.length == 2) //make absolutely sure THERE ARE TWO THINGS IN THIS ARRAY.
                    {
                        if(btWait.isShowing())
                        {
                            btWait.dismiss();
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                addMeasurement(Double.parseDouble(data[0]), Double.parseDouble(data[1]));

                                if(numMeasurements++ >= 10)
                                {
                                    Log.d("qsf", "(Parse) hr is " + data[0] + ", tmp is " + data[1]);
                                    numMeasurements = 1;
                                    locationListener = new QSFLocationListener(locationFoundHandler, Double.parseDouble(data[0]), Double.parseDouble(data[1]));
                                    locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
                                }
                                else
                                {
                                    Log.d("qsf", "(Local) hr is " + data[0] + ", tmp is " + data[1]);
                                }
                            }
                        });
                    }
                }
                catch(Exception e){e.printStackTrace();}
            }



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

    public void onResume()
    {
        super.onResume();
        if(MetricsActivity.chosen_one != null)
        {
                btWait = ProgressDialog.show(getActivity(), "Please Wait",
                        "Connecting to your smart fashion device...", true);
            MetricsActivity.chosen_one.connectGatt(getActivity(), true, mGattCallback);
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }


    public DashboardFragment()
    {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dashboard_fragment_layout, container, false);
        setHasOptionsMenu(true);

        tempF = (TextView) rootView.findViewById(R.id.dashboard_temp_F);
        tempTrend = (TextView) rootView.findViewById(R.id.dashboard_temp_trend);
        heartBpm = (TextView) rootView.findViewById(R.id.dashboard_heart_bpm);
        heartTrend = (TextView) rootView.findViewById(R.id.dashboard_heart_trend);
        healthStatus = (TextView) rootView.findViewById(R.id.dashboard_health_meter);
        healthStatusLayout = (LinearLayout) rootView.findViewById(R.id.dashboard_health_meter_layout);
        healthStatusImg = (ImageView) rootView.findViewById(R.id.dashboard_health_meter_img);

        heartHistory = (WebView) rootView.findViewById(R.id.dashboard_heart_webview);
        heartHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        heartHistory.getSettings().setJavaScriptEnabled(true);
        heartHistory.getSettings().setAllowUniversalAccessFromFileURLs(true);

        tempHistory = (WebView) rootView.findViewById(R.id.dashboard_temp_webview);
        tempHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        tempHistory.getSettings().setJavaScriptEnabled(true);
        tempHistory.getSettings().setAllowUniversalAccessFromFileURLs(true);

        loadMetricsFromParse();

        return rootView;
    }
}
