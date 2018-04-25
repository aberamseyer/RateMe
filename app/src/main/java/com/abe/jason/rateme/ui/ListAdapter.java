package com.abe.jason.rateme.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.abe.jason.rateme.R;
import com.abe.jason.rateme.activity.FindNearbyDevices;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<FindNearbyDevices.User> {
    public ListAdapter(@NonNull Context context, ArrayList<FindNearbyDevices.User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FindNearbyDevices.User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_user, parent, false);
        }
        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.name);
        // Populate the data into the template view using the data object
        tvName.setText(user.getName());

        return convertView;
    }

}
