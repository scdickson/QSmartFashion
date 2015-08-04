package com.qualcomm.qsmartfashion.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.qualcomm.qsmartfashion.MetricsActivity;
import com.qualcomm.qsmartfashion.R;

/**
 * Created by sdickson on 8/3/15.
 */
public class DashboardFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "DashboardFragment";

    WebView heartHistory, tempHistory;
    View rootView;

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

    protected void takeSimulatedDataPoint() {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.simulated_dp_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Take Simulated Reading");

        final EditText simulatedHeart = (EditText) promptView.findViewById(R.id.simulated_dp_heart);
        final EditText simulatedTemp = (EditText) promptView.findViewById(R.id.simulated_dp_temp);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        ParseObject locObject = new ParseObject("DataMeasurement");
                        locObject.put("heartrate", Integer.parseInt(simulatedHeart.getText().toString()));
                        locObject.put("temperature", Integer.parseInt(simulatedTemp.getText().toString()));
                        locObject.put("user", ParseUser.getCurrentUser());
                        locObject.saveInBackground();
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

    public DashboardFragment()
    {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dashboard_fragment_layout, container, false);
        setHasOptionsMenu(true);
        heartHistory = (WebView) rootView.findViewById(R.id.dashboard_heart_webview);
        heartHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        heartHistory.getSettings().setJavaScriptEnabled(true);
        heartHistory.loadUrl("file:///android_asset/cv.html");

        tempHistory = (WebView) rootView.findViewById(R.id.dashboard_temp_webview);
        tempHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        tempHistory.getSettings().setJavaScriptEnabled(true);
        tempHistory.loadUrl("file:///android_asset/cv.html");
        return rootView;
    }
}
