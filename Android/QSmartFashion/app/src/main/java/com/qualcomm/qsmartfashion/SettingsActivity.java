package com.qualcomm.qsmartfashion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.qualcomm.qsmartfashion.Objects.Contact;
import com.qualcomm.qsmartfashion.adapters.ContactListAdapter;
import com.qualcomm.qsmartfashion.utils.UnitConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdickson on 7/26/15.
 */
public class SettingsActivity extends Activity implements View.OnClickListener
{
    Intent intent;
    Context context;
    enum SAVE_MODE {NEW_USER, EXISTING_USER};
    SAVE_MODE mode;
    public static final int PICK_CONTACT = 0;

    EditText height_ft, height_in, age, weight;
    TextView save, cancel;
    ListView contactsList;

    public static ArrayList<Contact> contacts = new ArrayList<Contact>();
    ContactListAdapter contactsAdapter;
    ProgressDialog progress;

    Handler newContactHandler = new Handler()
    {
        public void handleMessage(Message inputMessage)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            startActivityForResult(intent, PICK_CONTACT);
        }
    };

    Handler deleteContactHandler = new Handler()
    {
        public void handleMessage(Message inputMessage)
        {
            final Contact condemned = (Contact) inputMessage.getData().getSerializable("condemned");

            if(mode == SAVE_MODE.EXISTING_USER && condemned != null)
            {
                ParseUser currentUser = ((QSmartFashion) context.getApplicationContext()).parseUser;
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EmergencyContact");
                query.whereEqualTo("user", currentUser);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> contactList, ParseException e) {
                        if (e == null) {
                            for (ParseObject obj : contactList)
                            {
                                if(obj.getObjectId().equals(condemned.id))
                                {
                                    obj.deleteInBackground();
                                    break;
                                }
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.activity_settings);
        context = this;

        progress = new ProgressDialog(this);
        progress.setTitle("Saving");
        progress.setMessage("Saving your settings...");

        height_ft = (EditText) findViewById(R.id.settings_height_ft);
        height_in = (EditText) findViewById(R.id.settings_height_in);
        age = (EditText) findViewById(R.id.settings_age);
        weight = (EditText) findViewById(R.id.settings_weight);
        save = (TextView) findViewById(R.id.settings_save);
        save.setOnClickListener(this);
        cancel = (TextView) findViewById(R.id.settings_cancel);
        cancel.setOnClickListener(this);

        contactsList = (ListView) findViewById(R.id.contact_list);
        populateContactsList();


        intent = getIntent();
        if(intent.getStringExtra("email") != null && intent.getStringExtra("password") != null) //We need to finish creating a new user.
        {
            mode = SAVE_MODE.NEW_USER;
            cancel.setVisibility(View.GONE);
        }
        else //We're editing a current user.
        {
            mode = SAVE_MODE.EXISTING_USER;
            ParseUser currentUser = ((QSmartFashion) getApplicationContext()).parseUser;
            age.setText(currentUser.get("age").toString());
            weight.setText(String.valueOf(UnitConverter.kilogramsToPounds(currentUser.getDouble("weight"))));

            int[] height_imperial = UnitConverter.centimetersToFeetInches(currentUser.getDouble("height"));
            height_ft.setText(String.valueOf(height_imperial[0]));
            height_in.setText(String.valueOf(height_imperial[1]));
        }

    }

    private void populateContactsList()
    {
        contacts.clear();
        ParseUser currentUser = ((QSmartFashion) context.getApplicationContext()).parseUser;

        if(currentUser != null)
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("EmergencyContact");
            query.whereEqualTo("user", currentUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> contactList, ParseException e) {
                    if (e == null) {
                        for (ParseObject obj : contactList) {
                            Contact newContact = new Contact();
                            newContact.isEmpty = false;
                            newContact.id = obj.getObjectId();
                            newContact.name = obj.getString("name");
                            newContact.phone_number = obj.getString("phoneNumber");
                            contacts.add(newContact);
                        }
                        while (contacts.size() < Constants.MAX_CONTACTS) {
                            Contact emptyContact = new Contact();
                            emptyContact.id = null;
                            emptyContact.name = null;
                            emptyContact.phone_number = null;
                            contacts.add(emptyContact);
                        }

                        contactsAdapter = new ContactListAdapter(context, newContactHandler, deleteContactHandler);
                        contactsList.setAdapter(contactsAdapter);
                        if(progress.isShowing())
                        {
                            progress.dismiss();
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
        {
            while (contacts.size() < Constants.MAX_CONTACTS) {
                Contact emptyContact = new Contact();
                emptyContact.name = null;
                emptyContact.phone_number = null;
                contacts.add(emptyContact);
            }

            contactsAdapter = new ContactListAdapter(context, newContactHandler, deleteContactHandler);
            contactsList.setAdapter(contactsAdapter);
        }

    }

    public void onClick(View view)
    {
        if(view.equals(save))
        {
            switch(mode)
            {
                case NEW_USER:
                    progress.show();
                    registerNewUser();
                    break;
                case EXISTING_USER:
                    progress.show();
                    updateExistingUser();
                    break;
            }
        }
        else if(view.equals(cancel))
        {
            Intent intent = new Intent(this, TrackerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom);
        }
    }

    private void updateExistingUser()
    {
        ParseUser user = ((QSmartFashion) context.getApplicationContext()).parseUser;
        user.put("age", Integer.parseInt(age.getText().toString()));

        int[] height_imperial = {Integer.parseInt(height_ft.getText().toString()), Integer.parseInt(height_in.getText().toString())};
        user.put("height", UnitConverter.feetInchesToCentimeters(height_imperial));

        user.put("weight", UnitConverter.poundsToKilograms(Integer.parseInt(weight.getText().toString())));
        user.saveInBackground();
        saveContacts(user);
        populateContactsList();
    }

    private void registerNewUser()
    {
        ParseUser user = new ParseUser();
        user.setUsername(intent.getStringExtra("email"));
        user.setPassword(intent.getStringExtra("password"));
        user.setEmail(intent.getStringExtra("email"));
        user.put("age", Integer.parseInt(age.getText().toString()));

        int[] height_imperial = {Integer.parseInt(height_ft.getText().toString()), Integer.parseInt(height_in.getText().toString())};
        user.put("height", UnitConverter.feetInchesToCentimeters(height_imperial));

        user.put("weight", UnitConverter.poundsToKilograms(Integer.parseInt(weight.getText().toString())));

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(context, TrackerActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.slide_out, R.animator.slide_in);
                } else {
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

        saveContacts(user);
        populateContactsList();
    }

    private void saveContacts(ParseUser user)
    {
        for(Contact c : contacts)
        {
            if(!c.isEmpty)
            {
                ParseObject contact;

                if(c.id != null)
                {
                    contact = ParseObject.createWithoutData("EmergencyContact", c.id);
                }
                else
                {
                    contact = new ParseObject("EmergencyContact");
                    contact.put("user", user);
                }

                contact.put("name", c.name);
                contact.put("phoneNumber", c.phone_number);
                contact.saveInBackground();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_CONTACT){
            if(resultCode == RESULT_OK){
                Uri contactData = data.getData();
                Cursor cursor =  managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME));

                Contact newContact = new Contact();
                newContact.isEmpty = false;
                newContact.name = name;
                newContact.phone_number = number;

                int i;
                for(i = 0; i < contacts.size(); i++)
                {
                    if(contacts.get(i).isEmpty == true)
                    {
                        break;
                    }
                }
                contacts.remove(i);
                contacts.add(i, newContact);
                contactsAdapter.notifyDataSetChanged();
            }
        }
    }
}

