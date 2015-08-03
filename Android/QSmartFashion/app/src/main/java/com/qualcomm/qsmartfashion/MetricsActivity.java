package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.parse.ParseUser;
import com.qualcomm.qsmartfashion.fragments.ProfileFragment;
import com.qualcomm.qsmartfashion.fragments.SOSContactFragment;
import com.qualcomm.qsmartfashion.objects.Contact;

public class MetricsActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    QSmartFashion application;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public void onResume()
    {
        super.onResume();

        Intent callingIntent = getIntent();
        if(callingIntent.getStringExtra("email") != null && callingIntent.getStringExtra("password") != null) //We need to finish creating a new user.
        {
            String loginData[] = {callingIntent.getStringExtra("email"), callingIntent.getStringExtra("password")};
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ProfileFragment.newInstance(1,loginData))
                    .commit();

        }
        else
        {
            if (ParseUser.getCurrentUser() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0:
                mTitle = getString(R.string.title_section_dashboard);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                mTitle = getString(R.string.title_section_profile);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ProfileFragment.newInstance(position + 1, null))
                        .commit();
                break;
            case 2:
                mTitle = getString(R.string.title_section_devices);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 3:
                mTitle = getString(R.string.title_section_devices);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SOSContactFragment.newInstance(position + 1))
                        .commit();
                break;
            case 4:
                mTitle = getString(R.string.title_section_logout);
                ParseUser.getCurrentUser().logOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_dashboard);
                break;
            case 2:
                mTitle = getString(R.string.title_section_profile);
                break;
            case 3:
                mTitle = getString(R.string.title_section_devices);
                break;
            case 4:
                mTitle = getString(R.string.title_section_contacts);
                break;
            case 5:
                mTitle = getString(R.string.title_section_logout);
                break;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_metrics, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MetricsActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
