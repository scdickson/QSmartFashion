package com.qualcomm.qsmartfashion.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.qualcomm.qsmartfashion.MetricsActivity;
import com.qualcomm.qsmartfashion.QSmartFashion;
import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.utils.UnitConverter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sdickson on 8/2/15.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_SECTION_NUMBER = "ProfileFragment";
    enum SAVE_MODE {NEW_USER, EXISTING_USER};

    SAVE_MODE mode;
    View rootView;
    EditText name, weight, height_ft, height_in, birthdate;
    Spinner sex;
    CheckBox publicData;

    int mYear, mMonth, mDay;

    public static ProfileFragment newInstance(int sectionNumber, String signupData[])
    {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        if(signupData != null) {
            args.putString("email", signupData[0]);
            args.putString("password", signupData[1]);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment()
    {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.profile_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_save:
                switch(mode)
                {
                    case NEW_USER:
                        registerNewUser();
                        break;
                    case EXISTING_USER:
                        updateExistingUser();
                        break;
                }
                return true;
        }

        return false;
    }

    public void onClick(View view)
    {
        if(view.equals(birthdate))
        {
            DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            birthdate.setText((monthOfYear + 1) + "/"
                                    + dayOfMonth+ "/" + year);

                        }
                    }, mYear, mMonth, mDay);
            dpd.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false);
        setHasOptionsMenu(true);
        name = (EditText) rootView.findViewById(R.id.settings_name);
        weight = (EditText) rootView.findViewById(R.id.settings_weight);
        height_ft = (EditText) rootView.findViewById(R.id.settings_height_ft);
        height_in = (EditText) rootView.findViewById(R.id.settings_height_in);
        sex = (Spinner) rootView.findViewById(R.id.settings_sex);
        publicData = (CheckBox) rootView.findViewById(R.id.settings_public_data);
        birthdate = (EditText) rootView.findViewById(R.id.settings_birthdate);
        birthdate.setOnClickListener(this);

        Intent intent = getActivity().getIntent();
        if(intent.getStringExtra("email") != null && intent.getStringExtra("password") != null)
        {
            mode = SAVE_MODE.NEW_USER;
        }
        else
        {
            mode = SAVE_MODE.EXISTING_USER;;
        }


        if(mode == SAVE_MODE.EXISTING_USER)
        {
            ParseUser currentUser = ParseUser.getCurrentUser();
            name.setText(currentUser.getString("name"));
            if(currentUser.getString("sex") != null) {
                switch (currentUser.getString("sex")) {
                    case "M":
                        sex.setSelection(0);
                        break;
                    case "F":

                        sex.setSelection(1);
                        break;
                }
            }
            publicData.setChecked(currentUser.getBoolean("dataPublic"));
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            Date user_birthdate = currentUser.getDate("birthdate");
            birthdate.setText(dateFormat.format(user_birthdate));
            mYear = user_birthdate.getYear();
            mMonth = user_birthdate.getMonth();
            mDay = user_birthdate.getDay();
            weight.setText(String.valueOf(UnitConverter.kilogramsToPounds(currentUser.getDouble("weight"))));
            int[] height_imperial = UnitConverter.centimetersToFeetInches(currentUser.getDouble("height"));
            height_ft.setText(String.valueOf(height_imperial[0]));
            height_in.setText(String.valueOf(height_imperial[1]));
        }
        else
        {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MetricsActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void updateExistingUser()
    {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name.getText().toString());

        try {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            Date date = dateFormat.parse(birthdate.getText().toString());
            user.put("birthdate", date);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        user.put("dataPublic", publicData.isChecked());

        switch(sex.getSelectedItemPosition())
        {
            case 0:
                user.put("sex", "M");
                break;
            case 1:
                user.put("sex", "F");
                break;
        }

        int[] height_imperial = {Integer.parseInt(height_ft.getText().toString()), Integer.parseInt(height_in.getText().toString())};
        user.put("height", UnitConverter.feetInchesToCentimeters(height_imperial));

        user.put("weight", UnitConverter.poundsToKilograms(Integer.parseInt(weight.getText().toString())));
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void registerNewUser()
    {
        ParseUser user = new ParseUser();
        user.setUsername(getActivity().getIntent().getStringExtra("email"));
        user.setPassword(getActivity().getIntent().getStringExtra("password"));
        user.setEmail(getActivity().getIntent().getStringExtra("email"));
        user.put("name", name.getText().toString());

        try
        {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            Date date = dateFormat.parse(birthdate.getText().toString());
            user.put("birthdate", date);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        switch(sex.getSelectedItemPosition())
        {
            case 0:
                user.put("sex", "M");
                break;
            case 1:
                user.put("sex", "F");
                break;
        }


        int[] height_imperial = {Integer.parseInt(height_ft.getText().toString()), Integer.parseInt(height_in.getText().toString())};
        user.put("height", UnitConverter.feetInchesToCentimeters(height_imperial));

        user.put("weight", UnitConverter.poundsToKilograms(Integer.parseInt(weight.getText().toString())));
        user.put("dataPublic", publicData.isChecked());

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {


                } else {
                    new AlertDialog.Builder(getActivity())
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
