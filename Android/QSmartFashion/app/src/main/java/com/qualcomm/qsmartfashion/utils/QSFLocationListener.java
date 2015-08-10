package com.qualcomm.qsmartfashion.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by sdickson on 8/4/15.
 */
public class QSFLocationListener implements LocationListener
{
    Handler handler;
    double heartrate, temperature;

    public QSFLocationListener(Handler handler, double heartrate, double temperature)
    {
        this.handler = handler;
        this.heartrate = heartrate;
        this.temperature = temperature;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}

    public void onLocationChanged(final Location location)
    {
        try
        {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putDouble("lat", location.getLatitude());
            bundle.putDouble("lng", location.getLongitude());
            bundle.putDouble("heartrate", heartrate);
            bundle.putDouble("temperature", temperature);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
