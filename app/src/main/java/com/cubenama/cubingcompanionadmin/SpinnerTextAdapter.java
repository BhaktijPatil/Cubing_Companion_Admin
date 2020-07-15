package com.cubenama.cubingcompanionadmin;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerTextAdapter extends ArrayAdapter {

    private final String[] textArray;
    private final Activity activity;

    SpinnerTextAdapter(Activity activity, String[] text) {
        super(activity, R.layout.spinner_text, text);
        this.activity = activity;
        this.textArray = text;
    }



    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.spinner_text, null,true);
        TextView contentTextView = rowView.findViewById(R.id.spinnerSelectedItemTextView);
        contentTextView.setText(textArray[position]);

        return rowView;
    }
}
