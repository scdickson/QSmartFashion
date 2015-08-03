package com.qualcomm.qsmartfashion.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.qualcomm.qsmartfashion.Constants;
import com.qualcomm.qsmartfashion.MetricsActivity;
import com.qualcomm.qsmartfashion.QSmartFashion;
import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.adapters.ContactListAdapter;
import com.qualcomm.qsmartfashion.objects.Contact;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdickson on 8/2/15.
 */
public class SOSContactFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "SOSContactFragment";
    enum SAVE_MODE {NEW_USER, EXISTING_USER};
    SAVE_MODE mode;
    public static final int PICK_CONTACT = 0;

    View rootView;
    ListView contactsList;
    public static ArrayList<Contact> contacts = new ArrayList<Contact>();
    public static ContactListAdapter contactsAdapter;

    Handler deleteContactHandler = new Handler()
    {
        public void handleMessage(Message inputMessage)
        {
            final Contact condemned = (Contact) inputMessage.getData().getSerializable("condemned");

            if(condemned != null)
            {
                ParseUser currentUser = ParseUser.getCurrentUser();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EmergencyContact");
                query.whereEqualTo("user", currentUser);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> contactList, ParseException e) {
                        if (e == null) {
                            for (ParseObject obj : contactList) {
                                if (obj.getObjectId().equals(condemned.id)) {
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

    public static SOSContactFragment newInstance(int sectionNumber)
    {
        SOSContactFragment fragment = new SOSContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SOSContactFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.sos_contact_fragment_layout, container, false);
        setHasOptionsMenu(true);
        contactsList = (ListView) rootView.findViewById(R.id.sos_contact_list_view);
        Log.d("qsf", "POPULATING CL");
        populateContactsList();
        return rootView;
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
        inflater.inflate(R.menu.sos_contact_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.action_edit:
                contactsAdapter.toggleDeleteState();
                return true;
            case R.id.action_new:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, PICK_CONTACT);
                return true;
        }

        return false;
    }

    private void populateContactsList()
    {
        contacts.clear();
        ParseUser currentUser = ParseUser.getCurrentUser();

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
                            newContact.photo_id = obj.getInt("photo_id");
                            contacts.add(newContact);
                        }

                        contactsAdapter = new ContactListAdapter(rootView.getContext(), deleteContactHandler);
                        contactsList.setAdapter(contactsAdapter);
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private Bitmap queryContactImage(int imageDataRow) {
        Cursor c = getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {
                ContactsContract.CommonDataKinds.Photo.PHOTO
        }, ContactsContract.Data._ID + "=?", new String[] {
                Integer.toString(imageDataRow)
        }, null);
        byte[] imageBytes = null;
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0);
            }
            c.close();
        }

        if (imageBytes != null) {
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SOSContactFragment.PICK_CONTACT){
            if(resultCode == Activity.RESULT_OK){
                Uri contactData = data.getData();
                Cursor cursor =  getActivity().managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME));
                int photo_id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_ID));
                Bitmap photo = queryContactImage(photo_id);

                ParseObject contact = new ParseObject("EmergencyContact");
                final Contact newContact = new Contact();
                newContact.isEmpty = false;
                newContact.name = name;
                newContact.phone_number = number;
                newContact.photo_id = photo_id;

                contact.put("user", ParseUser.getCurrentUser());
                contact.put("name", name);
                contact.put("phoneNumber", number);

                if(photo != null)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out);
                    byte[] photoData = out.toByteArray();
                    ParseFile img = new ParseFile(System.currentTimeMillis() + ".png", photoData);
                    img.saveInBackground();
                    contact.put("photo", img);
                    contact.put("photo_id", photo_id);
                }

                contact.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e == null)
                        {
                            populateContactsList();
                        }
                        else
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}