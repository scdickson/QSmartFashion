package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.Set;

/**
 * Created by sdickson on 7/26/15.
 */
public class TrackerActivity extends Activity implements View.OnClickListener
{
    QSmartFashion application;

    TextView tmp_settings;

    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.activity_tracker);

        tmp_settings = (TextView) findViewById(R.id.tmp_settings);
        tmp_settings.setOnClickListener(this);
    }

    public void onResume()
    {
        super.onResume();
        application = (QSmartFashion) getApplicationContext();
        application.parseUser = ParseUser.getCurrentUser();
        if(application.parseUser == null)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
        }

    }

    public void onClick(View view)
    {
        if(view.equals(tmp_settings))
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
        }
    }
}
