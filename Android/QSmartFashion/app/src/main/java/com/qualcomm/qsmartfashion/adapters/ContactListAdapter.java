package com.qualcomm.qsmartfashion.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.qsmartfashion.Objects.Contact;
import com.qualcomm.qsmartfashion.R;
import com.qualcomm.qsmartfashion.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by sdickson on 7/29/15.
 */
public class ContactListAdapter extends BaseAdapter
{
    Context context;
    Handler newContactHandler;
    Handler deleteContactHandler;

    public ContactListAdapter(Context context, Handler newContactHandler, Handler deleteContactHandler)
    {
        this.context = context;
        this.newContactHandler = newContactHandler;
        this.deleteContactHandler = deleteContactHandler;
    }

    public int getCount()
    {
        return SettingsActivity.contacts.size();
    }

    public Object getItem(int position)
    {
        return SettingsActivity.contacts.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.contact_list_item, parent, false);

        final Contact contact = (Contact) getItem(position);
        LinearLayout contactLayout = (LinearLayout) itemView.findViewById(R.id.contact_layout);
        ImageView btnAdd = (ImageView) itemView.findViewById(R.id.contact_add);
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                newContactHandler.sendEmptyMessage(0);
            }
        });
        ImageView btnRemove = (ImageView) itemView.findViewById(R.id.contact_remove);
        btnRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SettingsActivity.contacts.remove(contact);
                SettingsActivity.contacts.add(new Contact());
                Message m = new Message();
                Bundle b = new Bundle();
                b.putSerializable("condemned", contact);
                m.setData(b);
                deleteContactHandler.sendMessage(m);
                notifyDataSetChanged();
            }
        });
        TextView txtName = (TextView) itemView.findViewById(R.id.contact_name);
        TextView txtPhone = (TextView) itemView.findViewById(R.id.contact_phone);

        if(contact.isEmpty == true)
        {
            contactLayout.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);
        }
        else
        {
            contactLayout.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.GONE);

            txtName.setText(contact.name);
            txtPhone.setText(contact.phone_number);
        }

        return itemView;
    }
}
