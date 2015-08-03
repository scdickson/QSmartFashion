package com.qualcomm.qsmartfashion.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.qsmartfashion.fragments.SOSContactFragment;
import com.qualcomm.qsmartfashion.objects.Contact;
import com.qualcomm.qsmartfashion.R;

/**
 * Created by sdickson on 7/29/15.
 */
public class ContactListAdapter extends BaseAdapter
{
    Context context;
    Handler deleteContactHandler;
    boolean deleteState = false;
    boolean doSlideAnimation = false;
    Animation slideIn;
    Animation slideOut;
    Animation fadeOut;

    public ContactListAdapter(Context context, Handler deleteContactHandler)
    {
        this.context = context;
        this.deleteContactHandler = deleteContactHandler;
        slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out);
        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(0);
        fadeOut.setDuration(400);
    }

    public int getCount()
    {
        return SOSContactFragment.contacts.size();
    }

    public Object getItem(int position)
    {
        return SOSContactFragment.contacts.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    private Bitmap queryContactImage(int imageDataRow) {
        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {
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

    public void toggleDeleteState()
    {
        deleteState = !deleteState;
        doSlideAnimation = true;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.contact_list_item, parent, false);

        final Contact contact = (Contact) getItem(position);
        LinearLayout contactLayout = (LinearLayout) itemView.findViewById(R.id.contact_layout);
        ImageView imgPhoto = (ImageView) itemView.findViewById(R.id.contact_photo);
        TextView txtName = (TextView) itemView.findViewById(R.id.contact_name);
        TextView txtPhone = (TextView) itemView.findViewById(R.id.contact_phone);
        final LinearLayout deleteLayout = (LinearLayout) itemView.findViewById(R.id.contact_delete_layout);
        final LinearLayout contactLayoutParent = (LinearLayout) itemView.findViewById(R.id.contact_layout_parent);
        TextView btnDelete = (TextView) itemView.findViewById(R.id.contact_delete);

        txtName.setText(contact.name);
        txtPhone.setText(contact.phone_number);

        if(contact.photo_id > 0)
        {
            imgPhoto.setImageBitmap(queryContactImage(contact.photo_id));
        }

        if(deleteState)
        {
            deleteLayout.setVisibility(View.VISIBLE);
            if(doSlideAnimation) {
                deleteLayout.startAnimation(slideIn);
            }
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SOSContactFragment.contacts.remove(contact);
                    final Message m = new Message();
                    Bundle b = new Bundle();
                    b.putSerializable("condemned", contact);
                    m.setData(b);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            doSlideAnimation = false;
                            deleteContactHandler.sendMessage(m);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    contactLayoutParent.startAnimation(fadeOut);
                }
            });
        }

        return itemView;
    }
}
