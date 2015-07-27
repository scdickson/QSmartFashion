package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;

/**
 * Created by sdickson on 7/26/15.
 */
public class TrackerActivity extends Activity
{
    QSmartFashion application;

    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.activity_tracker);
    }

    public void onResume()
    {
        super.onResume();
        application = (QSmartFashion) getApplicationContext();
        application.currentUser = ParseUser.getCurrentUser();
        if(application.currentUser == null)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
        }
    }
}
