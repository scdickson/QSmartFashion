package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.qualcomm.qsmartfashion.utils.UnitConverter;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ParseUser user = new ParseUser();
        user.setUsername("sam");
        user.setPassword("kpcofgs");
        user.setEmail("scdickso@purdue.edu");
        user.put("age", 21);
        user.put("height", 188);
        user.put("weight", 69.85);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("QSmartFashion", "SIGNUP OK");
                } else {
                    e.printStackTrace();
                }
            }
        });*/

    }

}
