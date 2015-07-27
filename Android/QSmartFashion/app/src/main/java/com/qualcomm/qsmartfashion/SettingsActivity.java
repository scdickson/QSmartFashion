package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by sdickson on 7/26/15.
 */
public class SettingsActivity extends Activity implements View.OnClickListener
{
    Intent intent;
    Context context;

    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.activity_settings);
        context = this;

        intent = getIntent();
        if(intent.getStringExtra("email") != null && intent.getStringExtra("password") != null) //We need to finish creating a new user.
        {

        }
        else //We're editing a current user.
        {

        }

    }

    public void onClick(View view)
    {

    }

    private void registerNewUser()
    {
        ParseUser user = new ParseUser();
        user.setUsername(intent.getStringExtra("email"));
        user.setPassword(intent.getStringExtra("password"));
        user.setEmail(intent.getStringExtra("email"));
        user.put("age", 21);
        user.put("height", 188);
        user.put("weight", 69.85);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null)
                {
                    Intent intent = new Intent(context, TrackerActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.slide_out, R.animator.slide_in);
                }
                else
                {
                    new AlertDialog.Builder(context)
                            .setTitle(getResources().getString(R.string.signup_error_dialog_title))
                            .setMessage(getResources().getString(R.string.signup_error_dialog_message))
                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
    }
}
