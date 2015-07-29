package com.qualcomm.qsmartfashion;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseUser;

/**
 * Created by sdickson on 7/26/15.
 */
public class QSmartFashion extends Application
{
    public ParseUser parseUser;

    public void onCreate()
    {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Constants.PARSE_APPLICATION_KEY, Constants.PARSE_CLIENT_KEY);
    }
}
