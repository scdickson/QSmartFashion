package com.qualcomm.qsmartfashion.objects;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by sdickson on 7/29/15.
 */
public class Contact implements Serializable
{
    public boolean isEmpty = true;
    public String id;
    public String name = "";
    public String phone_number = "";
    public int photo_id = -1;

    public boolean equals(Object other)
    {
        Contact c = (Contact) other;

        if(id != null)
        {
            return c.id.equals(id);
        }

        return c.name.equals(name) && c.phone_number.equals(phone_number);
    }

    public String toString()
    {
        return "Contact " + id + ": (" + name + ", " + phone_number + ")";
    }
}
