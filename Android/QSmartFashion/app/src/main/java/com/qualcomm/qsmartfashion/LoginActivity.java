package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends Activity implements View.OnClickListener
{
    EditText email, password;
    TextView login, signup;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        email = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        login = (TextView) findViewById(R.id.login_login);
        login.setOnClickListener(this);
        signup = (TextView) findViewById(R.id.login_signup);
        signup.setOnClickListener(this);
    }

    public void onClick(View view)
    {
        if(view.equals(login))
        {
            if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty())
            {
                ParseUser.logInInBackground(email.getText().toString(), password.getText().toString(), new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            Intent intent = new Intent(context, MetricsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.animator.slide_out_to_bottom, R.animator.slide_in_from_bottom);
                        } else {
                            new AlertDialog.Builder(context)
                                    .setTitle(getResources().getString(R.string.login_error_dialog_title))
                                    .setMessage(getResources().getString(R.string.login_error_dialog_message))
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
        else if(view.equals(signup))
        {
            if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty())
            {
                Intent intent = new Intent(context, MetricsActivity.class);
                intent.putExtra("email", email.getText().toString());
                intent.putExtra("password", password.getText().toString());
                startActivity(intent);
            }
            else
            {
                new AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Please enter your email and password before signing up.")
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

}
